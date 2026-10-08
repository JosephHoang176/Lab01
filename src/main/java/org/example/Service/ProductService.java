package org.example.Service;

import org.example.Entity.Product;
import org.example.Repository.ProductJPARepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class ProductService {

    static final String ALL_PRODUCTS_CACHE_KEY = "products:all";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final ProductJPARepository productRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * The one-argument constructor keeps this service usable by non-Spring callers.
     * Spring uses the cache-enabled constructor.
     */
    public ProductService(ProductJPARepository productRepository) {
        this(productRepository, null, null);
    }

    @Autowired
    public ProductService(ProductJPARepository productRepository,
                          StringRedisTemplate redisTemplate,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Product> getAllProduct() {
        List<Product> cachedProducts = readCachedProducts();
        if (cachedProducts != null) {
            return cachedProducts;
        }

        List<Product> products = productRepository.findAll();
        writeCachedProducts(products);
        return products;
    }

    private List<Product> readCachedProducts() {
        if (redisTemplate == null || objectMapper == null) {
            return null;
        }
        try {
            String cached = redisTemplate.opsForValue().get(ALL_PRODUCTS_CACHE_KEY);
            if (cached == null || cached.isBlank()) {
                return null;
            }
            return objectMapper.readValue(cached, new TypeReference<>() {});
        } catch (Exception e) {
            // Redis is an optimization; a cache outage must not make products unavailable.
            return null;
        }
    }

    private void writeCachedProducts(List<Product> products) {
        if (redisTemplate == null || objectMapper == null) {
            return;
        }
        try {
            String serialized = objectMapper.writeValueAsString(products);
            redisTemplate.opsForValue().set(ALL_PRODUCTS_CACHE_KEY, serialized, CACHE_TTL);
        } catch (Exception ignored) {
            // The database remains the source of truth when Redis cannot be written.
        }
    }

    private void invalidateProductsCache() {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(ALL_PRODUCTS_CACHE_KEY);
        } catch (RuntimeException ignored) {
            // Cache invalidation is best effort and must not hide a successful write.
        }
    }

    public Product getProductById(int id) {
        validateId(id);
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "San pham khong ton tai: " + id));
    }

    public boolean createProduct(Product product) {
        if (!isValidProduct(product)) {
            return false;
        }
        if (productRepository.existsById(product.getId())) {
            return false;
        }
        productRepository.save(product);
        invalidateProductsCache();
        return true;
    }

    public boolean updateProduct(Product product) {
        if (!isValidProduct(product)
                || !productRepository.existsById(product.getId())) {
            return false;
        }
        productRepository.save(product);
        invalidateProductsCache();
        return true;
    }

    public boolean deleteProductById(int id) {
        if (id <= 0 || !productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        invalidateProductsCache();
        return true;
    }


    /*Cac ham tien ich */
    private boolean isValidProduct(Product product) {
        if (product == null || product.getId() <= 0) {
            return false;
        }
        return hasText(product.getSku())
                && product.getSku().length() <= 100
                && hasText(product.getName())
                && product.getName().length() <= 200
                && (product.getCategory() == null
                || product.getCategory().length() <= 100)
                && product.getUnitPrice() >= 0
                && product.getStock() >= 0
                && product.getCurrency() != null
                && product.getCurrency().matches("[A-Z]{3}");
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id khong hop le");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

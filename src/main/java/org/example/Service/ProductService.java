package org.example.Service;

import org.example.Entity.Product;
import org.example.Repository.ProductJPARepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductJPARepository productRepository;

    public ProductService(ProductJPARepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
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
        return true;
    }

    public boolean updateProduct(Product product) {
        if (!isValidProduct(product)
                || !productRepository.existsById(product.getId())) {
            return false;
        }
        productRepository.save(product);
        return true;
    }

    public boolean deleteProductById(int id) {
        if (id <= 0 || !productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
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

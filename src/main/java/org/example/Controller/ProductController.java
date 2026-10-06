package org.example.Controller;

import jakarta.validation.Valid;
import org.example.DTO.request.ProductDTO;
import org.example.Entity.Product;
import org.example.Mapping.ProductMapper;
import org.example.Service.ProductService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productService.getAllProduct();
        return products.stream().map(ProductMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ProductDTO getProductById(@PathVariable int id) {
        Product product = productService.getProductById(id);
        return ProductMapper.toDto(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean createProduct(@Valid @RequestBody ProductDTO dto) {
        Product product = ProductMapper.toEntity(dto);
        return productService.createProduct(product);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean updateProduct(@PathVariable int id, @Valid @RequestBody ProductDTO dto) {
        Product product = ProductMapper.toEntity(dto);
        product.setId(id);
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteProductById(@PathVariable int id) {
        return productService.deleteProductById(id);
    }
}

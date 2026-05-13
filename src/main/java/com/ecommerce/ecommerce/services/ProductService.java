package com.ecommerce.ecommerce.services;

import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2.0)
    )
    @Transactional
    public boolean purchaseProduct(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() >= quantity) {
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);  // هنا يحدث check على الـ version
            return true;
        }
        return false;
    }

    @Recover
    public boolean recover(OptimisticLockingFailureException exception, Long productId, int quantity) {
        return false;
    }
}

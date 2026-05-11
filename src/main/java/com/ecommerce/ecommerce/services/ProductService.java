package com.ecommerce.ecommerce.services;

import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.Sale;
import com.ecommerce.ecommerce.model.SaleItem;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.SaleRepository;
import com.ecommerce.ecommerce.repository.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

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

    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional
    public boolean purchaseProduct(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() >= quantity) {
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product); 

        
            Sale sale = new Sale();
            sale.setSaleDate(LocalDateTime.now());
            sale.setTotalAmount(product.getPrice() * quantity);
            saleRepository.save(sale);

            
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(quantity);
            saleItem.setPriceAtPurchase(product.getPrice());
            saleItemRepository.save(saleItem);

            return true;
        }
        return false;
    }
}
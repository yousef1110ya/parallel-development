package com.ecommerce.ecommerce.order.dto;

import java.math.BigDecimal;

public class OrderItemResponseDTO {

    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;

    public OrderItemResponseDTO(Long productId, String productName,
                                 int quantity, BigDecimal priceAtPurchase,
                                 BigDecimal subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
        this.subtotal = subtotal;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    public BigDecimal getSubtotal() { return subtotal; }
}
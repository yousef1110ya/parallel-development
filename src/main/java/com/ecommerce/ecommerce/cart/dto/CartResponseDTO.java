package com.ecommerce.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponseDTO {

    private Long cartId;
    private List<CartItemResponseDTO> items;
    private BigDecimal grandTotal;

    public CartResponseDTO(Long cartId, List<CartItemResponseDTO> items, BigDecimal grandTotal) {
        this.cartId = cartId;
        this.items = items;
        this.grandTotal = grandTotal;
    }

    public Long getCartId() { return cartId; }
    public List<CartItemResponseDTO> getItems() { return items; }
    public BigDecimal getGrandTotal() { return grandTotal; }
}
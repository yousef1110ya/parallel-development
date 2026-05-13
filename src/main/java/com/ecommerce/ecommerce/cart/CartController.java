package com.ecommerce.ecommerce.cart;

import com.ecommerce.ecommerce.cart.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET /api/cart
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    // POST /api/cart/items
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequestDTO dto) {
        return ResponseEntity.ok(cartService.addToCart(userDetails.getUsername(), dto));
    }

    // PUT /api/cart/items/{productId}
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequestDTO dto) {
        return ResponseEntity.ok(cartService.updateCartItem(userDetails.getUsername(), productId, dto));
    }

    // DELETE /api/cart/items/{productId}
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeCartItem(userDetails.getUsername(), productId));
    }

    // DELETE /api/cart
    @DeleteMapping
    public ResponseEntity<CartResponseDTO> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.clearCart(userDetails.getUsername()));
    }

    // POST /api/cart/checkout
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.checkout(userDetails.getUsername()));
    }
}
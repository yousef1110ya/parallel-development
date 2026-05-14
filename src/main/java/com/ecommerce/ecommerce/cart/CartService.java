package com.ecommerce.ecommerce.cart;

import com.ecommerce.ecommerce.cart.dto.*;
import com.ecommerce.ecommerce.order.*;
import com.ecommerce.ecommerce.product.Product;
import com.ecommerce.ecommerce.product.ProductRepository;
import com.ecommerce.ecommerce.transaction.Transaction;
import com.ecommerce.ecommerce.transaction.TransactionRepository;
import com.ecommerce.ecommerce.transaction.TransactionType;
import com.ecommerce.ecommerce.users.User;
import com.ecommerce.ecommerce.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository,
                       OrderRepository orderRepository,
                       TransactionRepository transactionRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
    }

    // Get or create cart for user
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    public CartResponseDTO getCart(String email) {
        User user = findUserOrThrow(email);
        Cart cart = getOrCreateCart(user);
        return toDTO(cart);
    }

    @Transactional
    public CartResponseDTO addToCart(String email, AddToCartRequestDTO dto) {
        User user = findUserOrThrow(email);
        Cart cart = getOrCreateCart(user);
        Product product = findProductOrThrow(dto.getProductId());

        // Stock validation
        if (dto.getQuantity() > product.getStock()) {
            throw new RuntimeException(
                "Insufficient stock for product: " + product.getName() +
                ". Available: " + product.getStock()
            );
        }

        // If item already exists, update quantity
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(existingItem -> {
                    int newQty = existingItem.getQuantity() + dto.getQuantity();
                    if (newQty > product.getStock()) {
                        throw new RuntimeException(
                            "Insufficient stock for product: " + product.getName() +
                            ". Available: " + product.getStock()
                        );
                    }
                    existingItem.setQuantity(newQty);
                    cartItemRepository.save(existingItem);
                }, () -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(dto.getQuantity());
                    cartItemRepository.save(item);
                });

        return toDTO(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponseDTO updateCartItem(String email, Long productId, UpdateCartItemRequestDTO dto) {
        User user = findUserOrThrow(email);
        Cart cart = getOrCreateCart(user);
        Product product = findProductOrThrow(productId);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (dto.getQuantity() > product.getStock()) {
            throw new RuntimeException(
                "Insufficient stock for product: " + product.getName() +
                ". Available: " + product.getStock()
            );
        }

        item.setQuantity(dto.getQuantity());
        cartItemRepository.save(item);
        return toDTO(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponseDTO removeCartItem(String email, Long productId) {
        User user = findUserOrThrow(email);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cartItemRepository.delete(item);
        return toDTO(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponseDTO clearCart(String email) {
        User user = findUserOrThrow(email);
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
        return toDTO(cart);
    }

    @Transactional
    public CheckoutResponseDTO checkout(String email) {
        User user = findUserOrThrow(email);
        Cart cart = getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<String> skippedItems = new ArrayList<>();
        List<OrderItem> validOrderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Skip if out of stock
            if (product.getStock() < cartItem.getQuantity()) {
                skippedItems.add(product.getName() +
                    " (requested: " + cartItem.getQuantity() +
                    ", available: " + product.getStock() + ")"
                );
                continue;
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            validOrderItems.add(orderItem);
        }

        // All items were skipped
        if (validOrderItems.isEmpty()) {
            throw new RuntimeException("No items could be fulfilled. All items are out of stock.");
        }

        // Balance check
        if (user.getBalance().compareTo(total) < 0) {
            throw new RuntimeException(
                "Insufficient balance. Required: " + total +
                ", Available: " + user.getBalance()
            );
        }

        // Deduct balance
        user.setBalance(user.getBalance().subtract(total));
        userRepository.save(user);

        // Deduct stock
        for (OrderItem orderItem : validOrderItems) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() - orderItem.getQuantity());
            productRepository.save(product);
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        for (OrderItem orderItem : validOrderItems) {
            orderItem.setOrder(order);
        }
        order.setItems(validOrderItems);
        orderRepository.save(order);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(total);
        transaction.setType(TransactionType.PURCHASE);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        String message = skippedItems.isEmpty()
                ? "Checkout successful"
                : "Checkout partially successful. Some items were skipped.";

        return new CheckoutResponseDTO(order.getId(), total, skippedItems, message);
    }

    // ---- Helpers ----

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    private CartResponseDTO toDTO(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(item -> new CartItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .collect(Collectors.toList());

        BigDecimal grandTotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(cart.getId(), itemDTOs, grandTotal);
    }
}

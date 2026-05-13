package com.ecommerce.ecommerce.order;

import com.ecommerce.ecommerce.order.dto.OrderItemResponseDTO;
import com.ecommerce.ecommerce.order.dto.OrderResponseDTO;
import com.ecommerce.ecommerce.users.User;
import com.ecommerce.ecommerce.users.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // Get own order history
    public List<OrderResponseDTO> getMyOrders(String email) {
        User user = findUserOrThrow(email);
        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Get single order by ID — user can only see own, admin sees all
    public OrderResponseDTO getOrderById(Long orderId, String email) {
        User user = findUserOrThrow(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = order.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return toDTO(order);
    }

    // Admin — get all orders
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ---- Helpers ----

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public OrderResponseDTO toDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems()
                .stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getPriceAtPurchase()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                itemDTOs,
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
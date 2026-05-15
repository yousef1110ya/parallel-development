package com.ecommerce.ecommerce.cart;

import com.ecommerce.ecommerce.category.Category;
import com.ecommerce.ecommerce.category.CategoryRepository;
import com.ecommerce.ecommerce.order.OrderRepository;
import com.ecommerce.ecommerce.order.OrderStatus;
import com.ecommerce.ecommerce.product.Product;
import com.ecommerce.ecommerce.product.ProductRepository;
import com.ecommerce.ecommerce.support.IntegrationTestSupport;
import com.ecommerce.ecommerce.transaction.TransactionRepository;
import com.ecommerce.ecommerce.users.User;
import com.ecommerce.ecommerce.users.UserRepository;
import com.ecommerce.ecommerce.users.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class CartServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

    @Autowired private CartService cartService;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    private Long productId;
    private final List<String> shopperEmails = new ArrayList<>();

    @BeforeEach
    void setUp() {
        shopperEmails.clear();

        Category category = new Category();
        category.setName("Concurrency");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Single Stock Product");
        product.setDescription("Used for optimistic locking tests");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(1);
        product.setCategory(category);
        product = productRepository.save(product);
        productId = product.getId();

        for (int i = 0; i < 20; i++) {
            User user = new User();
            user.setName("Shopper " + i);
            user.setEmail("shopper" + i + "@example.com");
            user.setPassword("password123");
            user.setRole(UserRole.USER);
            user.setBalance(new BigDecimal("1000.00"));
            user = userRepository.save(user);
            shopperEmails.add(user.getEmail());

            Cart cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItemRepository.save(cartItem);
        }
    }

    @Test
    void checkout_shouldAllowOnlyOneSuccessfulPurchase_whenStockIsOne() throws Exception {
        int threadCount = shopperEmails.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (String email : shopperEmails) {
            futures.add(executor.submit(runCheckout(email, ready, start)));
        }

        assertTrue(ready.await(10, TimeUnit.SECONDS));
        start.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        Product refreshedProduct = productRepository.findById(productId).orElseThrow();
        long successfulOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.SUCCESS)
                .count();

        assertEquals(1, successCount);
        assertEquals(0, refreshedProduct.getStock());
        assertEquals(1, successfulOrders);
        assertEquals(1, transactionRepository.findAll().size());
    }

    private Callable<Boolean> runCheckout(String email, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            start.await();
            try {
                cartService.checkout(email);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        };
    }
}

package com.ecommerce.ecommerce.cart;

import com.ecommerce.ecommerce.support.IntegrationTestSupport;
import com.ecommerce.ecommerce.users.UserRepository;
import com.ecommerce.ecommerce.users.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CartControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private String userToken;
    private String adminToken;
    private Long productId;
    private Long lowStockProductId;

    @BeforeEach
    void setUp() throws Exception {
        userToken = registerAndGetToken("Jane", "jane@example.com", "password123", false);
        adminToken = registerAndGetToken("Admin", "admin@example.com", "admin123", true);
        Long categoryId = createCategory("Electronics");
        productId = createProduct("Laptop", 500.00, 10, categoryId);
        lowStockProductId = createProduct("Rare Item", 100.00, 1, categoryId);
    }

    // ---- GET /api/cart ----

    @Test
    void getCart_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.grandTotal").value(0));
    }

    @Test
    void getCart_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    // ---- POST /api/cart/items ----

    @Test
    void addToCart_shouldReturn200_whenValidRequest() throws Exception {
        Map<String, Object> body = Map.of("productId", productId, "quantity", 2);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.grandTotal").value(1000.00));
    }

    @Test
    void addToCart_shouldUpdateQuantity_whenProductAlreadyInCart() throws Exception {
        Map<String, Object> body = Map.of("productId", productId, "quantity", 2);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(4));
    }

    @Test
    void addToCart_shouldReturn400_whenQuantityExceedsStock() throws Exception {
        Map<String, Object> body = Map.of("productId", productId, "quantity", 999);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- PUT /api/cart/items/{productId} ----

    @Test
    void updateCartItem_shouldReturn200_whenValidRequest() throws Exception {
        addProductToCart(productId, 2);

        Map<String, Object> body = Map.of("quantity", 5);
        mockMvc.perform(put("/api/cart/items/" + productId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    void updateCartItem_shouldReturn400_whenItemNotInCart() throws Exception {
        Map<String, Object> body = Map.of("quantity", 1);
        mockMvc.perform(put("/api/cart/items/9999")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /api/cart/items/{productId} ----

    @Test
    void removeCartItem_shouldReturn200_withEmptyCart() throws Exception {
        addProductToCart(productId, 2);

        mockMvc.perform(delete("/api/cart/items/" + productId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ---- DELETE /api/cart ----

    @Test
    void clearCart_shouldReturn200_withEmptyCart() throws Exception {
        addProductToCart(productId, 2);

        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.grandTotal").value(0));
    }

    // ---- POST /api/cart/checkout ----

    @Test
    void checkout_shouldReturn200_whenBalanceSufficient() throws Exception {
        depositBalance(1000.00);
        addProductToCart(productId, 1);

        mockMvc.perform(post("/api/cart/checkout")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCharged").value(500.00))
                .andExpect(jsonPath("$.message").value("Checkout successful"));
    }

    @Test
    void checkout_shouldReturn400_whenBalanceInsufficient() throws Exception {
        depositBalance(10.00);
        addProductToCart(productId, 1);

        mockMvc.perform(post("/api/cart/checkout")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_shouldSkipOutOfStockItems_andNotifyUser() throws Exception {
        depositBalance(1000.00);
        addProductToCart(productId, 1);          // stock: 10 — valid
        addProductToCart(lowStockProductId, 1);  // stock: 1 — valid at add time

        // Drain low stock product's stock by buying it as admin scenario
        // Instead directly set stock to 0 via update product endpoint
        Map<String, Object> updateBody = Map.of(
                "name", "Rare Item",
                "price", 100.00,
                "stock", 0,
                "categoryId", getCategoryIdForProduct(lowStockProductId)
        );
        mockMvc.perform(put("/api/products/" + lowStockProductId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)));

        mockMvc.perform(post("/api/cart/checkout")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCharged").value(500.00))
                .andExpect(jsonPath("$.skippedItems").isArray())
                .andExpect(jsonPath("$.skippedItems[0]").value(org.hamcrest.Matchers.containsString("Rare Item")))
                .andExpect(jsonPath("$.message").value("Checkout partially successful. Some items were skipped."));
    }

    @Test
    void checkout_shouldReturn400_whenCartIsEmpty() throws Exception {
        mockMvc.perform(post("/api/cart/checkout")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    // ---- Helpers ----

    private void addProductToCart(Long pId, int quantity) throws Exception {
        Map<String, Object> body = Map.of("productId", pId, "quantity", quantity);
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void depositBalance(double amount) throws Exception {
        Map<String, Object> body = Map.of("amount", amount);
        mockMvc.perform(post("/api/users/me/deposit")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private Long getCategoryIdForProduct(Long pId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products/" + pId)
                        .header("Authorization", "Bearer " + userToken))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("category").get("id").asLong();
    }

    private Long createCategory(String name) throws Exception {
        Map<String, String> body = Map.of("name", name);
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createProduct(String name, double price, int stock, Long catId) throws Exception {
        Map<String, Object> body = Map.of("name", name, "price", price, "stock", stock, "categoryId", catId);
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String registerAndGetToken(String name, String email,
                                        String password, boolean makeAdmin) throws Exception {
        Map<String, String> body = Map.of("name", name, "email", email, "password", password);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        if (makeAdmin) {
            userRepository.findByEmail(email).ifPresent(u -> {
                u.setRole(UserRole.ADMIN);
                userRepository.save(u);
            });
            Map<String, String> loginBody = Map.of("email", email, "password", password);
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andReturn();
            return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        }

        return json.get("token").asText();
    }
}

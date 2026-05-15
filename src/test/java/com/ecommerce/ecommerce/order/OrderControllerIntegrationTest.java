package com.ecommerce.ecommerce.order;

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
class OrderControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private String userToken;
    private String adminToken;
    private String otherUserToken;
    private Long productId;
    private Long orderId;

    @BeforeEach
    void setUp() throws Exception {
        userToken = registerAndGetToken("Jane", "jane@example.com", "password123", false);
        adminToken = registerAndGetToken("Admin", "admin@example.com", "admin123", true);
        otherUserToken = registerAndGetToken("Bob", "bob@example.com", "password123", false);

        Long categoryId = createCategory("Electronics");
        productId = createProduct("Laptop", 300.00, 10, categoryId);

        // Deposit and checkout to generate an order
        depositBalance("jane@example.com", userToken, 1000.00);
        addToCart(userToken, productId, 1);
        orderId = checkout(userToken);
    }

    // ---- GET /api/orders/me ----

    @Test
    void getMyOrders_shouldReturn200_withOrderList() throws Exception {
        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].totalPrice").value(300.00))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].items[0].productName").value("Laptop"));
    }

    @Test
    void getMyOrders_shouldReturn200_withEmptyList_whenNoOrders() throws Exception {
        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyOrders_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---- GET /api/orders/{id} ----

    @Test
    void getOrderById_shouldReturn200_whenOwner() throws Exception {
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getOrderById_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void getOrderById_shouldReturn403_whenNotOwnerOrAdmin() throws Exception {
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrderById_shouldReturn400_whenOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/9999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /api/orders ----

    @Test
    void getAllOrders_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(orderId));
    }

    @Test
    void getAllOrders_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---- Helpers ----

    private Long checkout(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/cart/checkout")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("orderId").asLong();
    }

    private void addToCart(String token, Long pId, int quantity) throws Exception {
        Map<String, Object> body = Map.of("productId", pId, "quantity", quantity);
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void depositBalance(String email, String token, double amount) throws Exception {
        Map<String, Object> body = Map.of("amount", amount);
        mockMvc.perform(post("/api/users/me/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
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
        Map<String, Object> body = Map.of("name", name, "price", price,
                                          "stock", stock, "categoryId", catId);
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
            return objectMapper.readTree(
                    loginResult.getResponse().getContentAsString()).get("token").asText();
        }

        return json.get("token").asText();
    }
}

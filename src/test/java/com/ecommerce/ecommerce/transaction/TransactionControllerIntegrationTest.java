package com.ecommerce.ecommerce.transaction;

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
class TransactionControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userToken = registerAndGetToken("Jane", "jane@example.com", "password123", false);
        adminToken = registerAndGetToken("Admin", "admin@example.com", "admin123", true);
    }

    // ---- GET /api/transactions/me ----

    @Test
    void getMyTransactions_shouldReturn200_withEmptyList_whenNoTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyTransactions_shouldReturn200_withDepositTransaction() throws Exception {
        deposit(userToken, 200.00);

        mockMvc.perform(get("/api/transactions/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(200.00))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    void getMyTransactions_shouldReturn200_withPurchaseTransaction() throws Exception {
        Long categoryId = createCategory("Electronics");
        Long productId = createProduct("Phone", 150.00, 5, categoryId);
        deposit(userToken, 500.00);
        addToCart(userToken, productId, 1);
        checkout(userToken);

        mockMvc.perform(get("/api/transactions/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // index 0 = deposit, index 1 = purchase
                .andExpect(jsonPath("$[1].type").value("PURCHASE"))
                .andExpect(jsonPath("$[1].amount").value(150.00));
    }

    @Test
    void getMyTransactions_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/transactions/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyTransactions_shouldOnlyReturnOwnTransactions() throws Exception {
        deposit(userToken, 100.00);

        // Other user should see empty list
        String otherToken = registerAndGetToken("Bob", "bob@example.com", "pass123", false);
        mockMvc.perform(get("/api/transactions/me")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---- Helpers ----

    private void deposit(String token, double amount) throws Exception {
        Map<String, Object> body = Map.of("amount", amount);
        mockMvc.perform(post("/api/users/me/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void addToCart(String token, Long productId, int quantity) throws Exception {
        Map<String, Object> body = Map.of("productId", productId, "quantity", quantity);
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void checkout(String token) throws Exception {
        mockMvc.perform(post("/api/cart/checkout")
                .header("Authorization", "Bearer " + token));
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

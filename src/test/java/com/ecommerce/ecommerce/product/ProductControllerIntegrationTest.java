package com.ecommerce.ecommerce.product;


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
class ProductControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    private String userToken;
    private String adminToken;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        userToken = registerAndGetToken("Jane", "jane@example.com", "password123", false);
        adminToken = registerAndGetToken("Admin", "admin@example.com", "admin123", true);
        categoryId = createCategory("Electronics");
    }

    // ---- GET /api/products ----

    @Test
    void getAllProducts_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllProducts_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllProducts_shouldFilterByCategory() throws Exception {
        createProduct("Laptop", 999.99, 10, categoryId);

        mockMvc.perform(get("/api/products?categoryId=" + categoryId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    // ---- GET /api/products/{id} ----

    @Test
    void getProductById_shouldReturn200_whenExists() throws Exception {
        Long productId = createProduct("Phone", 499.99, 5, categoryId);

        mockMvc.perform(get("/api/products/" + productId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.category.name").value("Electronics"));
    }

    @Test
    void getProductById_shouldReturn400_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/products/9999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    // ---- POST /api/products ----

    @Test
    void createProduct_shouldReturn200_whenAdmin() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Tablet",
                "description", "A nice tablet",
                "price", 299.99,
                "stock", 20,
                "categoryId", categoryId
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tablet"))
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.category.name").value("Electronics"));
    }

    @Test
    void createProduct_shouldReturn403_whenNotAdmin() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Tablet",
                "description", "A nice tablet",
                "price", 299.99,
                "stock", 20,
                "categoryId", categoryId
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_shouldReturn400_whenStockIsNegative() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Tablet",
                "price", 299.99,
                "stock", -1,
                "categoryId", categoryId
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_shouldReturn400_whenCategoryNotFound() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Tablet",
                "price", 299.99,
                "stock", 10,
                "categoryId", 9999
        );

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- PUT /api/products/{id} ----

    @Test
    void updateProduct_shouldReturn200_whenAdmin() throws Exception {
        Long productId = createProduct("Old Name", 100.00, 5, categoryId);

        Map<String, Object> body = Map.of(
                "name", "New Name",
                "price", 150.00,
                "stock", 8,
                "categoryId", categoryId
        );

        mockMvc.perform(put("/api/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.price").value(150.00));
    }

    // ---- DELETE /api/products/{id} ----

    @Test
    void deleteProduct_shouldReturn204_whenAdmin() throws Exception {
        Long productId = createProduct("To Delete", 50.00, 3, categoryId);

        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_shouldReturn403_whenNotAdmin() throws Exception {
        Long productId = createProduct("To Delete", 50.00, 3, categoryId);

        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---- Helpers ----

    private Long createCategory(String name) throws Exception {
        Map<String, String> body = Map.of("name", name);
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private Long createProduct(String name, double price, int stock, Long catId) throws Exception {
        Map<String, Object> body = Map.of(
                "name", name,
                "price", price,
                "stock", stock,
                "categoryId", catId
        );
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
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
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setRole(UserRole.ADMIN);
                userRepository.save(user);
            });
            Map<String, String> loginBody = Map.of("email", email, "password", password);
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andReturn();
            JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
            return loginJson.get("token").asText();
        }

        return json.get("token").asText();
    }
}

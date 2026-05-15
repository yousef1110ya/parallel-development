package com.ecommerce.ecommerce.user;


import com.ecommerce.ecommerce.support.IntegrationTestSupport;
import com.ecommerce.ecommerce.users.UserRepository;
import com.ecommerce.ecommerce.users.UserRole;
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
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class UserControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userToken = registerAndGetToken("Jane Doe", "jane@example.com", "password123", false);
        adminToken = registerAndGetToken("Admin User", "admin@example.com", "admin123", true);
    }

    // ---- GET /api/users/me ----

    @Test
    void getProfile_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void getProfile_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---- PUT /api/users/me ----

    @Test
    void updateProfile_shouldReturn200_whenValidRequest() throws Exception {
        Map<String, String> body = Map.of("name", "Jane Updated");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Updated"));
    }

    @Test
    void updateProfile_shouldReturn400_whenNameBlank() throws Exception {
        Map<String, String> body = Map.of("name", "");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- POST /api/users/me/deposit ----

    @Test
    void deposit_shouldReturn200AndUpdatedBalance() throws Exception {
        Map<String, Object> body = Map.of("amount", 100.00);

        mockMvc.perform(post("/api/users/me/deposit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void deposit_shouldReturn400_whenAmountIsZero() throws Exception {
        Map<String, Object> body = Map.of("amount", 0.00);

        mockMvc.perform(post("/api/users/me/deposit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /api/users (Admin only) ----

    @Test
    void getAllUsers_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllUsers_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---- DELETE /api/users/{id} (Admin only) ----

    @Test
    void deleteUser_shouldReturn204_whenAdmin() throws Exception {
        // Get jane's ID from profile
        MvcResult result = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Long janeId = json.get("id").asLong();

        mockMvc.perform(delete("/api/users/" + janeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturn403_whenNotAdmin() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Long janeId = json.get("id").asLong();

        mockMvc.perform(delete("/api/users/" + janeId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---- Helper ----

    private String registerAndGetToken(String name, String email, String password, boolean makeAdmin) throws Exception {
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
            // Re-login to get admin token
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

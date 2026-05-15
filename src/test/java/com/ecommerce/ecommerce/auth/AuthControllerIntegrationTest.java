package com.ecommerce.ecommerce.auth;


import com.ecommerce.ecommerce.support.IntegrationTestSupport;
import com.ecommerce.ecommerce.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {}

    // ---- Register ----

    @Test
    void register_shouldReturn200AndToken_whenValidRequest() throws Exception {
        Map<String, String> body = Map.of(
                "name", "John Doe",
                "email", "john@example.com",
                "password", "secret123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_shouldReturn400_whenEmailAlreadyExists() throws Exception {
        Map<String, String> body = Map.of(
                "name", "John Doe",
                "email", "john@example.com",
                "password", "secret123"
        );

        // Register once
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        // Register again with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenFieldsMissing() throws Exception {
        Map<String, String> body = Map.of("name", "John Doe");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- Login ----

    @Test
    void login_shouldReturn200AndToken_whenValidCredentials() throws Exception {
        // Register first
        Map<String, String> registerBody = Map.of(
                "name", "John Doe",
                "email", "john@example.com",
                "password", "secret123"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk());

        // Now login
        Map<String, String> loginBody = Map.of(
                "email", "john@example.com",
                "password", "secret123"
        );
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void login_shouldReturn401_whenWrongPassword() throws Exception {
        Map<String, String> registerBody = Map.of(
                "name", "John Doe",
                "email", "john@example.com",
                "password", "secret123"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk());

        Map<String, String> loginBody = Map.of(
                "email", "john@example.com",
                "password", "wrongpassword"
        );
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isUnauthorized());
    }
}

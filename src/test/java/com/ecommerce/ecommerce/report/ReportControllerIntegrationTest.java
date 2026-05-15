package com.ecommerce.ecommerce.report;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ReportControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    // Mock EmailService so no real emails are sent during tests
    @MockBean private EmailService emailService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userToken = registerAndGetToken("Jane", "jane@example.com", "password123", false);
        adminToken = registerAndGetToken("Admin", "admin@example.com", "admin123", true);

        doNothing().when(emailService).sendEmailWithAttachment(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    // ---- POST /api/reports/transactions/weekly ----

    @Test
    void weeklyTransactionsReport_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(post("/api/reports/transactions/weekly")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Weekly transactions report sent successfully"));

        verify(emailService, times(1)).sendEmailWithAttachment(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.contains("Weekly Transactions"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void weeklyTransactionsReport_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/reports/transactions/weekly")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void weeklyTransactionsReport_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/reports/transactions/weekly"))
                .andExpect(status().isUnauthorized());
    }

    // ---- POST /api/reports/products/best-sellers ----

    @Test
    void bestSellersReport_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(post("/api/reports/products/best-sellers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Best sellers report sent successfully"));

        verify(emailService, times(1)).sendEmailWithAttachment(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.contains("Best Sellers"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void bestSellersReport_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/reports/products/best-sellers")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---- Helpers ----

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

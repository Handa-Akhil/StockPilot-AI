package com.stockpilot.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpilot.api.auth.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private JwtResponse jwtResponse;

    @BeforeEach
    public void setup() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@stockpilot.com");
        signupRequest.setPassword("password123");
        signupRequest.setName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@stockpilot.com");
        loginRequest.setPassword("password123");

        jwtResponse = new JwtResponse(
                "mock-access-token",
                "mock-refresh-token",
                1L,
                "test@stockpilot.com",
                "Test User"
        );
    }

    @Test
    public void registerUser_Success() throws Exception {
        Mockito.when(authService.registerUser(any(SignupRequest.class))).thenReturn(new User());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    public void authenticateUser_Success() throws Exception {
        Mockito.when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.email").value("test@stockpilot.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void refreshToken_Success() throws Exception {
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setToken("mock-refresh-token");

        TokenRefreshResponse refreshResponse = new TokenRefreshResponse("new-mock-access-token", "mock-refresh-token");
        Mockito.when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(refreshResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }
}

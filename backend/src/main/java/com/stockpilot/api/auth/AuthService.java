package com.stockpilot.api.auth;

import com.stockpilot.api.auth.dto.*;

public interface AuthService {
    User registerUser(SignupRequest signupRequest);
    JwtResponse authenticateUser(LoginRequest loginRequest);
    TokenRefreshResponse refreshToken(TokenRefreshRequest refreshRequest);
    void logoutUser(Long userId);
    UserResponse getProfile(String email);
}

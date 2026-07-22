package com.stockpilot.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private final String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String email;
    private String name;
}

package com.stockpilot.api.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}

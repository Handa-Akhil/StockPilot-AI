package com.stockpilot.api.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioCreateRequest {

    @NotBlank(message = "Portfolio name cannot be blank")
    @Size(max = 100, message = "Portfolio name cannot exceed 100 characters")
    private String name;
}

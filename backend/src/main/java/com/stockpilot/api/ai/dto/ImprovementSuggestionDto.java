package com.stockpilot.api.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementSuggestionDto {
    private String title;
    private String category;
    private String description;
    private String priority; // HIGH | MEDIUM | LOW
    private String actionText;
}

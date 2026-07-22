package com.stockpilot.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private final boolean success = false;
    private String code;
    private String message;
    private String timestamp;
    private String path;
}

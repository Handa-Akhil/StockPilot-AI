package com.stockpilot.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    private Boolean emailNotificationsEnabled;
    private Boolean realtimeNotificationsEnabled;
    private Boolean aiNotificationsEnabled;
}

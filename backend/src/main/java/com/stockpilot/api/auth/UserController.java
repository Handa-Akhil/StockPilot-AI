package com.stockpilot.api.auth;

import com.stockpilot.api.auth.dto.UserSettingsDto;
import com.stockpilot.api.auth.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/settings")
    public ResponseEntity<UserSettingsDto> getUserSettings(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userDetails.getId()));

        UserSettingsDto dto = UserSettingsDto.builder()
                .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                .realtimeNotificationsEnabled(user.getRealtimeNotificationsEnabled())
                .aiNotificationsEnabled(user.getAiNotificationsEnabled())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/settings")
    public ResponseEntity<UserSettingsDto> updateUserSettings(
            @RequestBody UserSettingsDto settings,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userDetails.getId()));

        if (settings.getEmailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(settings.getEmailNotificationsEnabled());
        }
        if (settings.getRealtimeNotificationsEnabled() != null) {
            user.setRealtimeNotificationsEnabled(settings.getRealtimeNotificationsEnabled());
        }
        if (settings.getAiNotificationsEnabled() != null) {
            user.setAiNotificationsEnabled(settings.getAiNotificationsEnabled());
        }

        User saved = userRepository.save(user);

        UserSettingsDto response = UserSettingsDto.builder()
                .emailNotificationsEnabled(saved.getEmailNotificationsEnabled())
                .realtimeNotificationsEnabled(saved.getRealtimeNotificationsEnabled())
                .aiNotificationsEnabled(saved.getAiNotificationsEnabled())
                .build();

        return ResponseEntity.ok(response);
    }
}

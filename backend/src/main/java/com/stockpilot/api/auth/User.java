package com.stockpilot.api.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    private String name;

    @Column(name = "email_notifications_enabled", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Column(name = "realtime_notifications_enabled", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean realtimeNotificationsEnabled = true;

    @Column(name = "ai_notifications_enabled", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean aiNotificationsEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled == null ? Boolean.TRUE : emailNotificationsEnabled;
    }

    public Boolean getRealtimeNotificationsEnabled() {
        return realtimeNotificationsEnabled == null ? Boolean.TRUE : realtimeNotificationsEnabled;
    }

    public Boolean getAiNotificationsEnabled() {
        return aiNotificationsEnabled == null ? Boolean.TRUE : aiNotificationsEnabled;
    }
}

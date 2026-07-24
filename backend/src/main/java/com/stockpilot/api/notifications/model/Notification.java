package com.stockpilot.api.notifications.model;

import com.stockpilot.api.auth.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "type", nullable = false, length = 50, columnDefinition = "varchar(50) default 'PRICE_ALERT'")
    @Builder.Default
    private String type = "PRICE_ALERT";

    @Column(name = "read_status", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean readStatus = false;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "ai_explanation", length = 2000)
    private String aiExplanation;

    public Boolean getReadStatus() {
        return readStatus == null ? Boolean.FALSE : readStatus;
    }
}

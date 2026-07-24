package com.stockpilot.api.alerts.model;

import com.stockpilot.api.auth.User;
import com.stockpilot.api.portfolio.model.Portfolio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = true)
    private Portfolio portfolio;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    private AlertCondition condition;

    @Column(name = "target_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetPrice;

    @Column(name = "enabled", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "triggered", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean triggered = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Boolean getEnabled() {
        return enabled == null ? Boolean.TRUE : enabled;
    }

    public Boolean getTriggered() {
        return triggered == null ? Boolean.FALSE : triggered;
    }
}

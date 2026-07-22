package com.stockpilot.api.portfolio.model;

import com.stockpilot.api.auth.User;
import com.stockpilot.api.common.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "portfolios",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

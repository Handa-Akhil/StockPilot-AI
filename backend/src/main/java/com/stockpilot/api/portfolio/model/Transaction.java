package com.stockpilot.api.portfolio.model;

import com.stockpilot.api.common.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Portfolio portfolio;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false, length = 20)
    private AssetClass assetClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price;

    @Column(name = "realized_pl", precision = 18, scale = 4)
    private BigDecimal realizedPl;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;
}

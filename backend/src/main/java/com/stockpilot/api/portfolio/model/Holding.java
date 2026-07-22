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

@Entity
@Table(
    name = "portfolio_holdings",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"portfolio_id", "symbol"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding extends BaseAuditEntity {

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

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(name = "average_buy_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal averageBuyPrice;
}

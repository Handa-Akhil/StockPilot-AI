package com.stockpilot.api.alerts.repository;

import com.stockpilot.api.alerts.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<PriceAlert> findAllByEnabledTrueAndTriggeredFalse();
    Optional<PriceAlert> findByIdAndUserId(Long id, Long userId);
}

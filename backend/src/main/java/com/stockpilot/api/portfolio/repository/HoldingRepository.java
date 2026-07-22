package com.stockpilot.api.portfolio.repository;

import com.stockpilot.api.portfolio.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    
    @Query("SELECT h FROM Holding h WHERE h.portfolio.id = :portfolioId AND h.portfolio.user.id = :userId AND h.portfolio.deletedAt IS NULL")
    List<Holding> findAllByPortfolioIdAndUserId(Long portfolioId, Long userId);

    @Query("SELECT h FROM Holding h WHERE h.portfolio.id = :portfolioId AND h.symbol = :symbol AND h.portfolio.user.id = :userId AND h.portfolio.deletedAt IS NULL")
    Optional<Holding> findByPortfolioIdAndSymbolAndUserId(Long portfolioId, String symbol, Long userId);
}

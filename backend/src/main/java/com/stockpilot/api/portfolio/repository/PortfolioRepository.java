package com.stockpilot.api.portfolio.repository;

import com.stockpilot.api.portfolio.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    List<Portfolio> findAllActiveByUserId(Long userId);

    @Query("SELECT p FROM Portfolio p WHERE p.id = :id AND p.user.id = :userId AND p.deletedAt IS NULL")
    Optional<Portfolio> findActiveByIdAndUserId(Long id, Long userId);
}

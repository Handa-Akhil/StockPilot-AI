package com.stockpilot.api.portfolio.repository;

import com.stockpilot.api.portfolio.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.portfolio.id = :portfolioId AND t.portfolio.user.id = :userId AND t.portfolio.deletedAt IS NULL")
    Page<Transaction> findAllByPortfolioIdAndUserId(Long portfolioId, Long userId, Pageable pageable);
}

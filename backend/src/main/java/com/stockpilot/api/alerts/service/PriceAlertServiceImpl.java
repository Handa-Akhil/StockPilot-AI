package com.stockpilot.api.alerts.service;

import com.stockpilot.api.alerts.dto.PriceAlertRequest;
import com.stockpilot.api.alerts.dto.PriceAlertResponse;
import com.stockpilot.api.alerts.model.PriceAlert;
import com.stockpilot.api.alerts.repository.PriceAlertRepository;
import com.stockpilot.api.auth.User;
import com.stockpilot.api.auth.UserRepository;
import com.stockpilot.api.portfolio.model.Portfolio;
import com.stockpilot.api.portfolio.repository.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceAlertServiceImpl implements PriceAlertService {

    private static final Logger log = LoggerFactory.getLogger(PriceAlertServiceImpl.class);

    private final PriceAlertRepository priceAlertRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public PriceAlertServiceImpl(
            PriceAlertRepository priceAlertRepository,
            UserRepository userRepository,
            PortfolioRepository portfolioRepository) {
        this.priceAlertRepository = priceAlertRepository;
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
    }

    @Override
    @Transactional
    public PriceAlertResponse createAlert(PriceAlertRequest request, Long userId) {
        log.info("Creating price alert for symbol {} condition {} target {} for user id={}",
                request.getSymbol(), request.getCondition(), request.getTargetPrice(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userId));

        Portfolio portfolio = null;
        if (request.getPortfolioId() != null) {
            portfolio = portfolioRepository.findActiveByIdAndUserId(request.getPortfolioId(), userId)
                    .orElse(null);
        }

        PriceAlert alert = PriceAlert.builder()
                .user(user)
                .portfolio(portfolio)
                .symbol(request.getSymbol().trim().toUpperCase())
                .condition(request.getCondition())
                .targetPrice(request.getTargetPrice())
                .enabled(true)
                .triggered(false)
                .build();

        PriceAlert saved = priceAlertRepository.save(alert);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PriceAlertResponse updateAlert(Long alertId, PriceAlertRequest request, Long userId) {
        log.info("Updating price alert id={} for user id={}", alertId, userId);
        PriceAlert alert = priceAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: id=" + alertId));

        alert.setSymbol(request.getSymbol().trim().toUpperCase());
        alert.setCondition(request.getCondition());
        alert.setTargetPrice(request.getTargetPrice());
        alert.setTriggered(false); // Reset trigger status on edit

        PriceAlert saved = priceAlertRepository.save(alert);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PriceAlertResponse toggleAlert(Long alertId, Long userId) {
        log.info("Toggling alert status for id={} user id={}", alertId, userId);
        PriceAlert alert = priceAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: id=" + alertId));

        alert.setEnabled(!alert.getEnabled());
        if (alert.getEnabled()) {
            alert.setTriggered(false); // Reset trigger status when re-enabled
        }

        PriceAlert saved = priceAlertRepository.save(alert);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAlert(Long alertId, Long userId) {
        log.info("Deleting price alert id={} for user id={}", alertId, userId);
        PriceAlert alert = priceAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: id=" + alertId));
        priceAlertRepository.delete(alert);
    }

    @Override
    public List<PriceAlertResponse> getUserAlerts(Long userId) {
        return priceAlertRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PriceAlertResponse mapToResponse(PriceAlert alert) {
        return PriceAlertResponse.builder()
                .id(alert.getId())
                .portfolioId(alert.getPortfolio() != null ? alert.getPortfolio().getId() : null)
                .symbol(alert.getSymbol())
                .condition(alert.getCondition())
                .targetPrice(alert.getTargetPrice())
                .enabled(alert.getEnabled())
                .triggered(alert.getTriggered())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}

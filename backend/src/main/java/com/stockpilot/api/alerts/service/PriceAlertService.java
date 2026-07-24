package com.stockpilot.api.alerts.service;

import com.stockpilot.api.alerts.dto.PriceAlertRequest;
import com.stockpilot.api.alerts.dto.PriceAlertResponse;

import java.util.List;

public interface PriceAlertService {
    PriceAlertResponse createAlert(PriceAlertRequest request, Long userId);
    PriceAlertResponse updateAlert(Long alertId, PriceAlertRequest request, Long userId);
    PriceAlertResponse toggleAlert(Long alertId, Long userId);
    void deleteAlert(Long alertId, Long userId);
    List<PriceAlertResponse> getUserAlerts(Long userId);
}

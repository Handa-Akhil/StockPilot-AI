package com.stockpilot.api.notifications.service;

import com.stockpilot.api.notifications.dto.NotificationResponse;
import com.stockpilot.api.notifications.model.Notification;
import com.stockpilot.api.notifications.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByTimestampDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadStatusFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                n.setReadStatus(true);
                notificationRepository.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> userNotifications = notificationRepository.findAllByUserIdOrderByTimestampDesc(userId);
        userNotifications.forEach(n -> n.setReadStatus(true));
        notificationRepository.saveAll(userNotifications);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                notificationRepository.delete(n);
            }
        });
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .readStatus(n.getReadStatus())
                .timestamp(n.getTimestamp())
                .aiExplanation(n.getAiExplanation())
                .build();
    }
}

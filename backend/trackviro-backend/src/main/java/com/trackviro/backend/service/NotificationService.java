package com.trackviro.backend.service;

import com.trackviro.backend.dto.notification.NotificationResponse;
import com.trackviro.backend.model.User;

import java.util.List;

/**
 * Ported from com.example.demo.service.NotificationService, adapted
 * to DTOs for the two read methods. createNotification still takes
 * the User entity directly — this is an internal, service-to-service
 * call (invoked by ExpenseServiceImpl/LimitRequestServiceImpl), never
 * called from a controller, so it stays entity-based rather than
 * wrapped in a request DTO that nothing would ever populate.
 */
public interface NotificationService {

    void createNotification(User employee, String message, String type);

    List<NotificationResponse> getNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAllRead(Long userId);
}

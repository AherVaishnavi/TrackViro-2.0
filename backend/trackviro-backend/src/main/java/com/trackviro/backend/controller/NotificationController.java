package com.trackviro.backend.controller;

import com.trackviro.backend.dto.common.ApiMessage;
import com.trackviro.backend.dto.notification.NotificationResponse;
import com.trackviro.backend.security.AuthUtil;
import com.trackviro.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces com.example.demo.controller.NotificationController.
 *
 * Kept under "/api/employee/**" rather than a separate top-level
 * prefix: the old app's NotificationServiceImpl.createNotification()
 * is only ever called with an expense's employee as the recipient
 * (see ExpenseServiceImpl/LimitRequestServiceImpl from Step 4) —
 * managers and finance never receive notifications in this
 * application. Giving this its own "/api/notifications/**" prefix
 * would need a new, broader security rule for something that, in
 * practice, only employees ever call. This mirrors the old app's own
 * single endpoint: "/employee/notifications/markRead".
 */
@RestController
@RequestMapping("/api/employee/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    public NotificationController(NotificationService notificationService, AuthUtil authUtil) {
        this.notificationService = notificationService;
        this.authUtil = authUtil;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotifications(authUtil.currentUserId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount(authUtil.currentUserId()));
    }

    @PostMapping("/mark-read")
    public ResponseEntity<ApiMessage> markAllRead() {
        notificationService.markAllRead(authUtil.currentUserId());
        return ResponseEntity.ok(new ApiMessage("All notifications marked as read."));
    }
}

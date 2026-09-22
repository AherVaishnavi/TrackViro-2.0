package com.trackviro.backend.dto.notification;

import com.trackviro.backend.model.Notification;
import java.time.LocalDateTime;

/** No nested User object — the notification list is always fetched
 *  for "the current user", so their identity is already known from
 *  the request context and doesn't need repeating per-row. */
public record NotificationResponse(
        Long id,
        String message,
        String type,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        if (n == null) return null;
        return new NotificationResponse(
                n.getId(),
                n.getMessage(),
                n.getType(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}

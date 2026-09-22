package com.trackviro.backend.repository;

import com.trackviro.backend.model.Notification;
import com.trackviro.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Ported unchanged from com.example.demo.repository.NotificationRepository. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
    long countByUserAndIsReadFalse(User user);
}

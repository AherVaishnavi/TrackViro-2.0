package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.notification.NotificationResponse;
import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.Notification;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.NotificationRepository;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ported from com.example.demo.service.impl.NotificationServiceImpl.
 * Logic unchanged — the only addition is resolving userId -> User via
 * the repository (findById + ResourceNotFoundException) since the old
 * controller passed a session-loaded User directly and no session
 * exists here.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;

    @Override
    @Transactional
    public void createNotification(User employee, String message, String type) {
        Notification n = new Notification();
        n.setUser(employee);
        n.setMessage(message);
        n.setType(type);
        notificationRepository.save(n);
    }

    @Override
    public List<NotificationResponse> getNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(NotificationResponse::from).toList();
    }

    @Override
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}

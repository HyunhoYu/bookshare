package my.domain.notification.service;

import my.domain.notification.NotificationVO;

import java.util.List;

public interface NotificationService {
    void create(Long userId, String type, String message, Long referenceId);
    List<NotificationVO> findByUserId(Long userId);
    void markAsRead(Long notificationId, Long userId);
    int countUnread(Long userId);
}

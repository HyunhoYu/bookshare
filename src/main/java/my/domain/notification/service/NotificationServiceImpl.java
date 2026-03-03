package my.domain.notification.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.notification.NotificationMapper;
import my.domain.notification.NotificationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void create(Long userId, String type, String message, Long referenceId) {
        NotificationVO vo = new NotificationVO();
        vo.setUserId(userId);
        vo.setType(type);
        vo.setMessage(message);
        vo.setReferenceId(referenceId);

        int result = notificationMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.NOTIFICATION_INSERT_FAIL);
        }
    }

    @Override
    public List<NotificationVO> findByUserId(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        NotificationVO notification = requireNonNull(
                notificationMapper.selectById(notificationId),
                ErrorCode.NOTIFICATION_NOT_FOUND
        );

        if (!notification.getUserId().equals(userId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        notificationMapper.markAsRead(notificationId);
    }

    @Override
    public int countUnread(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }
}

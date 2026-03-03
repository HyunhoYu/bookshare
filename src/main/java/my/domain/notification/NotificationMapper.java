package my.domain.notification;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insert(NotificationVO vo);
    List<NotificationVO> selectByUserId(Long userId);
    int markAsRead(Long id);
    int countUnreadByUserId(Long userId);
    NotificationVO selectById(Long id);
}

package my.domain.notification;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;

@Getter
@Setter
public class NotificationVO extends MyApplicationVO {
    private Long userId;
    private String type;
    private String message;
    private Long referenceId;
    private Integer isRead;
    private Timestamp createdAt;
}

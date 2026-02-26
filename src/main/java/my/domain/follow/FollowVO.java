package my.domain.follow;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;

@Getter
@Setter
public class FollowVO extends MyApplicationVO {
    private Long customerId;
    private Long bookOwnerId;
    private Timestamp createdAt;

    // JOIN fields
    private String bookOwnerName;
    private String bookOwnerNickname;
}

package my.domain.post_like;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;

@Getter
@Setter
public class PostLikeVO extends MyApplicationVO {
    private Long postId;
    private Long userId;
    private Timestamp createdAt;
}

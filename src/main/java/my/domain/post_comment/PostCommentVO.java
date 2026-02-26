package my.domain.post_comment;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class PostCommentVO extends MyApplicationVO {
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    // JOIN fields
    private String userName;
    private String userRole;

    // 대댓글 목록 (최상위 댓글 조회 시)
    private List<PostCommentVO> replies;
}

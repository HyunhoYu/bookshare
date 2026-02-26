package my.domain.bookowner_post;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;

@Getter
@Setter
public class BookOwnerPostVO extends MyApplicationVO {
    private Long bookOwnerId;
    private Long bookId;
    private String title;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    // JOIN fields
    private String bookOwnerName;
    private String bookOwnerNickname;
    private String bookName;
}

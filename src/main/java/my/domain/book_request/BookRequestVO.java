package my.domain.book_request;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;

@Getter
@Setter
public class BookRequestVO extends MyApplicationVO {
    private Long customerId;
    private String isbn;
    private String bookTitle;
    private String author;
    private String publisher;
    private String thumbnailUrl;
    private String status;
    private String adminComment;
    private Timestamp createdAt;
    private Timestamp processedAt;

    // JOIN fields
    private String customerName;
}

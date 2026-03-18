package my.domain.review;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;
import java.sql.Timestamp;

@Getter
@Setter
public class PurchaseReviewVO extends MyApplicationVO {
    private Long bookSaleRecordId;
    private Long customerId;
    private Long bookOwnerId;
    private int rating;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // JOIN fields
    private String customerName;
    private String bookName;
    private String bookOwnerName;
}

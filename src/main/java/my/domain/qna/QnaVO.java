package my.domain.qna;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;

@Getter
@Setter
public class QnaVO extends MyApplicationVO {
    private Long customerId;
    private String title;
    private String content;
    private String answer;
    private Long answeredBy;
    private Timestamp answeredAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // JOIN fields
    private String customerName;
}

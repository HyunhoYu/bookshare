package my.domain.booksoldrecord.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

@Getter
@Setter
public class BookSoldRecordVO extends MyApplicationVO {

    private Long id;
    private Timestamp soldAt;
    private int soldPrice;
    private Long customerId;
    private String groupCodeId;   // common_code_group.group_code (예: "BUY_TYPE")
    private String commonCodeId;  // common_code.code (예: "01"=신용카드, "02"=현금)
    private Long bookOwnerSettlementId;
}

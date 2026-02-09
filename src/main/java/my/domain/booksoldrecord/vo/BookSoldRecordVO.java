package my.domain.booksoldrecord.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

@Getter
@Setter
public class BookSoldRecordVO extends MyApplicationVO {

    private Timestamp soldAt;
    private int soldPrice;
    private Long customerId;
    private String commonCodeId;  // common_code.code (예: "01"=신용카드, "02"=현금)
    private Long bookOwnerSettlementId; //이거는 정산할때 초기화되는 컬럼
    private Long ratioId;

}

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
    private String groupCodeId;
    private String commonCodeId;
    private Long bookOwnerSettlementId;
    private Long ratioId;

    // JOIN fields
    private Long bookOwnerId;
    private String bookName;
    private String bookOwnerName;
    private String buyTypeName;
    private Double ownerRatio;
    private Double storeRatio;

}

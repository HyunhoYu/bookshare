package my.domain.settlement.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;


@Getter
@Setter
public class SettlementVO extends MyApplicationVO {
    private Long bookOwnerId;
    private Timestamp settledAt;
    private int totalAmount;
    private int ownerAmount;
    private int storeAmount;
    private String payoutKey;
    private String transferStatus;
    private int deductedRentalAmount;
    private int actualPayoutAmount;

    // JOIN field
    private String bookOwnerName;
}

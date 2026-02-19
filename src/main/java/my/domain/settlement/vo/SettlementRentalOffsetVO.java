package my.domain.settlement.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettlementRentalOffsetVO {
    private Long id;
    private Long settlementId;
    private Long rentalSettlementId;
    private int offsetAmount;
    private Timestamp createdAt;
}

package my.domain.deposit;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DepositRentalOffsetVO {
    private Long id;
    private Long depositId;
    private Long rentalSettlementId;
    private Integer offsetAmount;
    private LocalDateTime createdAt;
}

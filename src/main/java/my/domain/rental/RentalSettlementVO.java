package my.domain.rental;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RentalSettlementVO {
    private Long id;
    private Long occupiedRecordId;
    private Long bookOwnerId;
    private String targetMonth;
    private Integer amount;
    private String status;
    private Integer deductedAmount;
    private Integer remainingAmount;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}

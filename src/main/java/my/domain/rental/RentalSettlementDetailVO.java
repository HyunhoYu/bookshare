package my.domain.rental;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RentalSettlementDetailVO {
    // RENTAL_SETTLEMENT
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

    // BOOK_CASE
    private Long bookCaseId;
    private String locationCode;
    private String locationName;

    // BOOK_CASE_TYPE
    private String bookCaseTypeCode;
}

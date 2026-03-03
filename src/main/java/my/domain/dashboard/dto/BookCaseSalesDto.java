package my.domain.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseSalesDto {
    private Long bookCaseId;
    private String locationName;
    private String bookCaseTypeCode;
    private Long bookOwnerId;
    private String bookOwnerName;
    private int soldCount;
    private long totalSalesAmount;
}

package my.domain.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SalesSummaryDto {
    private String targetMonth;
    private long totalSalesAmount;
    private int totalSoldCount;
    private int bookCaseCount;
    private long averageSalesPerBookCase;
    private List<BookCaseSalesDto> bookCaseSales;
}

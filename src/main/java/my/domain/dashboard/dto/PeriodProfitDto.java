package my.domain.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeriodProfitDto {
    private String startDate;
    private String endDate;
    private long totalSales;
    private long ownerTotal;
    private long operatingProfit;
    private int soldCount;
}

package my.domain.dashboard;

import my.domain.dashboard.dto.BookCaseSalesDto;
import my.domain.dashboard.dto.BookOwnerRankingDto;
import my.domain.dashboard.dto.PeriodProfitDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

    List<BookCaseSalesDto> selectBookCaseSales(@Param("startMonth") String startMonth, @Param("endMonth") String endMonth);

    List<BookOwnerRankingDto> selectBookOwnerRankingByFollowers(@Param("startMonth") String startMonth, @Param("endMonth") String endMonth);

    List<BookOwnerRankingDto> selectBookOwnerRankingBySales(@Param("startMonth") String startMonth, @Param("endMonth") String endMonth);

    PeriodProfitDto selectPeriodProfit(@Param("startDate") String startDate, @Param("endDate") String endDate);
}

package my.domain.dashboard;

import my.domain.dashboard.dto.BookCaseSalesDto;
import my.domain.dashboard.dto.BookOwnerRankingDto;
import my.domain.dashboard.dto.PeriodProfitDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

    List<BookCaseSalesDto> selectBookCaseSalesByMonth(@Param("targetMonth") String targetMonth);

    List<BookOwnerRankingDto> selectBookOwnerRankingByFollowers();

    List<BookOwnerRankingDto> selectBookOwnerRankingBySales();

    PeriodProfitDto selectPeriodProfit(@Param("startDate") String startDate, @Param("endDate") String endDate);
}

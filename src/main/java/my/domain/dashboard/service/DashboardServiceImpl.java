package my.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import my.domain.book_request.BookRequestMapper;
import my.domain.book_request.BookRequestVO;
import my.domain.dashboard.DashboardMapper;
import my.domain.dashboard.dto.BookCaseSalesDto;
import my.domain.dashboard.dto.BookOwnerRankingDto;
import my.domain.dashboard.dto.PeriodProfitDto;
import my.domain.dashboard.dto.SalesSummaryDto;
import my.domain.qna.QnaMapper;
import my.domain.qna.QnaVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final BookRequestMapper bookRequestMapper;
    private final QnaMapper qnaMapper;

    @Override
    public SalesSummaryDto getSalesSummary(String targetMonth) {
        if (targetMonth == null || targetMonth.isEmpty()) {
            targetMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<BookCaseSalesDto> bookCaseSales = dashboardMapper.selectBookCaseSalesByMonth(targetMonth);

        long totalSalesAmount = bookCaseSales.stream()
                .mapToLong(BookCaseSalesDto::getTotalSalesAmount)
                .sum();

        int totalSoldCount = bookCaseSales.stream()
                .mapToInt(BookCaseSalesDto::getSoldCount)
                .sum();

        int bookCaseCount = bookCaseSales.size();
        long averageSalesPerBookCase = bookCaseCount > 0 ? totalSalesAmount / bookCaseCount : 0;

        SalesSummaryDto summary = new SalesSummaryDto();
        summary.setTargetMonth(targetMonth);
        summary.setTotalSalesAmount(totalSalesAmount);
        summary.setTotalSoldCount(totalSoldCount);
        summary.setBookCaseCount(bookCaseCount);
        summary.setAverageSalesPerBookCase(averageSalesPerBookCase);
        summary.setBookCaseSales(bookCaseSales);

        return summary;
    }

    @Override
    public List<BookOwnerRankingDto> getBookOwnerRanking(String sort) {
        if ("sales".equals(sort)) {
            return dashboardMapper.selectBookOwnerRankingBySales();
        }
        return dashboardMapper.selectBookOwnerRankingByFollowers();
    }

    @Override
    public List<BookRequestVO> getRecentBookRequests(int limit) {
        List<BookRequestVO> all = bookRequestMapper.selectAll();
        if (all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    @Override
    public List<QnaVO> getRecentQna(int limit) {
        return qnaMapper.selectRecent(limit);
    }

    @Override
    public PeriodProfitDto getPeriodProfit(String startDate, String endDate) {
        PeriodProfitDto result = dashboardMapper.selectPeriodProfit(startDate, endDate);
        if (result == null) {
            result = new PeriodProfitDto();
        }
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        return result;
    }
}

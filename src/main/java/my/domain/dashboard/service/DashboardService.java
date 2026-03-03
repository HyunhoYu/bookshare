package my.domain.dashboard.service;

import my.domain.book_request.BookRequestVO;
import my.domain.dashboard.dto.BookOwnerRankingDto;
import my.domain.dashboard.dto.PeriodProfitDto;
import my.domain.dashboard.dto.SalesSummaryDto;
import my.domain.qna.QnaVO;

import java.util.List;

public interface DashboardService {
    SalesSummaryDto getSalesSummary(String targetMonth);
    List<BookOwnerRankingDto> getBookOwnerRanking(String sort);
    List<BookRequestVO> getRecentBookRequests(int limit);
    List<QnaVO> getRecentQna(int limit);
    PeriodProfitDto getPeriodProfit(String startDate, String endDate);
}

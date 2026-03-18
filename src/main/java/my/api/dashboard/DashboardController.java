package my.api.dashboard;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book_request.BookRequestVO;
import my.domain.dashboard.dto.BookOwnerRankingDto;
import my.domain.dashboard.dto.PeriodProfitDto;
import my.domain.dashboard.dto.SalesSummaryDto;
import my.domain.dashboard.service.DashboardService;
import my.domain.qna.QnaVO;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @RequireRole(Role.ADMIN)
    @GetMapping("/bookcase-sales")
    public ApiResponse<SalesSummaryDto> getSalesSummary(
            @RequestParam(value = "startMonth", required = false) String startMonth,
            @RequestParam(value = "endMonth", required = false) String endMonth) {
        return ApiResponse.success(dashboardService.getSalesSummary(startMonth, endMonth));
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/book-owner-ranking")
    public ApiResponse<List<BookOwnerRankingDto>> getBookOwnerRanking(
            @RequestParam(value = "sort", defaultValue = "popularity") String sort,
            @RequestParam(value = "startMonth", required = false) String startMonth,
            @RequestParam(value = "endMonth", required = false) String endMonth) {
        return ApiResponse.success(dashboardService.getBookOwnerRanking(sort, startMonth, endMonth));
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/recent-book-requests")
    public ApiResponse<List<BookRequestVO>> getRecentBookRequests(
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ApiResponse.success(dashboardService.getRecentBookRequests(limit));
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/recent-qna")
    public ApiResponse<List<QnaVO>> getRecentQna(
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ApiResponse.success(dashboardService.getRecentQna(limit));
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/period-profit")
    public ApiResponse<PeriodProfitDto> getPeriodProfit(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return ApiResponse.success(dashboardService.getPeriodProfit(startDate, endDate));
    }
}

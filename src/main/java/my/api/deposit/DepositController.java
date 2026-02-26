package my.api.deposit;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.deposit.DepositMapper;
import my.domain.deposit.DepositRentalOffsetMapper;
import my.domain.deposit.DepositRentalOffsetVO;
import my.domain.deposit.DepositVO;
import my.domain.deposit.service.DepositService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits")
public class DepositController {

    private final DepositMapper depositMapper;
    private final DepositRentalOffsetMapper depositRentalOffsetMapper;
    private final DepositService depositService;

    @RequireRole(Role.ADMIN)
    @GetMapping("/book-owner/{bookOwnerId}")
    public ApiResponse<DepositVO> findByBookOwner(@PathVariable("bookOwnerId") Long bookOwnerId) {
        DepositVO deposit = depositMapper.selectByBookOwnerId(bookOwnerId);
        if (deposit == null) {
            return ApiResponse.notFound("보증금 정보가 없습니다");
        }
        return ApiResponse.success(deposit);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/{depositId}/offsets")
    public ApiResponse<List<DepositRentalOffsetVO>> findOffsets(@PathVariable("depositId") Long depositId) {
        List<DepositRentalOffsetVO> offsets = depositRentalOffsetMapper.selectByDepositId(depositId);
        return ApiResponse.success(offsets);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/process-overdue")
    public ApiResponse<Void> processMonthlyOverdue() {
        depositService.processMonthlyOverdue();
        return ApiResponse.success(null);
    }

    @RequireRole(Role.BOOK_OWNER)
    @GetMapping("/my")
    public ApiResponse<DepositVO> findMyDeposit(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DepositVO deposit = depositMapper.selectByBookOwnerId(userId);
        if (deposit == null) {
            return ApiResponse.notFound("보증금 정보가 없습니다");
        }
        return ApiResponse.success(deposit);
    }

    @RequireRole(Role.BOOK_OWNER)
    @GetMapping("/my/offsets")
    public ApiResponse<List<DepositRentalOffsetVO>> findMyOffsets(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DepositVO deposit = depositMapper.selectByBookOwnerId(userId);
        if (deposit == null) {
            return ApiResponse.notFound("보증금 정보가 없습니다");
        }
        List<DepositRentalOffsetVO> offsets = depositRentalOffsetMapper.selectByDepositId(deposit.getId());
        return ApiResponse.success(offsets);
    }
}

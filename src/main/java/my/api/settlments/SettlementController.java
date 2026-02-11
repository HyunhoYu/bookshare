package my.api.settlments;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.settlement.dto.SettlementRequestDto;
import my.domain.settlement.service.SettlementService;
import my.domain.settlement.vo.SettlementVO;
import my.enums.Role;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @RequireRole(Role.ADMIN)
    @GetMapping("/pending")
    public ApiResponse<List<BookSoldRecordVO>> findAllUnsettled() {
        List<BookSoldRecordVO> unsettled = settlementService.findAllUnsettled();
        return ApiResponse.success(unsettled);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping
    public ApiResponse<SettlementVO> settle(@RequestBody @Valid SettlementRequestDto requestDto) {
        SettlementVO settlement = settlementService.settle(requestDto);
        return ApiResponse.created(settlement);
    }
}

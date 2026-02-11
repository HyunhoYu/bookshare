package my.api.settlement_ratio;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.settlement_ratio.dto.SettlementRatioRequestDto;
import my.domain.settlement_ratio.service.SettlementRatioService;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import jakarta.validation.Valid;
import my.enums.Role;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements/ratio")
public class SettlementRatioController {
    private final SettlementRatioService settlementRatioService;

    @RequireRole(Role.ADMIN)
    @PostMapping("/set")
    public ApiResponse<SettlementRatioVO> create(@RequestBody @Valid SettlementRatioRequestDto dto) {
        SettlementRatioVO vo = new SettlementRatioVO();
        vo.setOwnerRatio(dto.getOwnerRatio());
        vo.setStoreRatio(dto.getStoreRatio());

        settlementRatioService.create(vo);
        SettlementRatioVO result = settlementRatioService.findCurrentRatio();
        return ApiResponse.success(result);
    }

}

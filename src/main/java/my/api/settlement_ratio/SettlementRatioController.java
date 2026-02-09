package my.api.settlement_ratio;

import lombok.RequiredArgsConstructor;
import my.common.response.ApiResponse;
import my.domain.settlement_ratio.service.SettlementRatioService;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements/ratio")
public class SettlementRatioController {
    private final SettlementRatioService settlementRatioService;

    @PostMapping("/set")
    public ApiResponse<SettlementRatioVO> setSettlementService(@RequestBody SettlementRatioVO settlementRatioVO) {

        settlementRatioService.setRatio(settlementRatioVO);
        SettlementRatioVO result = settlementRatioService.getRatio();
        return ApiResponse.success(result);
    }

}

package my.domain.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.domain.settlement.service.SettlementService;
import my.domain.settlement.vo.SettlementVO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final SettlementService settlementService;

    @Scheduled(cron = "0 0 6 1 * *")
    public void monthlySettlement() {
        log.info("월초 배치 정산 시작");
        List<SettlementVO> results = settlementService.settleAll();
        log.info("월초 배치 정산 완료 - {}건 처리", results.size());
    }
}

package my.domain.deposit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.domain.deposit.service.DepositService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositScheduler {

    private final DepositService depositService;

    @Scheduled(cron = "0 0 5 1 * *", zone = "Asia/Seoul")
    public void monthlyOverdueProcess() {
        log.info("보증금 연체 공제 스케줄러 시작");
        depositService.processMonthlyOverdue();
        log.info("보증금 연체 공제 스케줄러 완료");
    }
}

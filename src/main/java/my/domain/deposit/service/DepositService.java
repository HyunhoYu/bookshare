package my.domain.deposit.service;

public interface DepositService {

    /**
     * 매월 1일 05:00 스케줄러에서 호출.
     * 활성 점유 전체를 대상으로 연체 임대료 보증금 공제 + 중지/강제퇴거 처리.
     */
    void processMonthlyOverdue();
}

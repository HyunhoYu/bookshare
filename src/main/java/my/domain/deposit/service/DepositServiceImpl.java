package my.domain.deposit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.domain.bookcase.BookCaseOccupiedRecordMapper;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.service.BookCaseService;
import my.domain.deposit.DepositMapper;
import my.domain.deposit.DepositRentalOffsetMapper;
import my.domain.deposit.DepositRentalOffsetVO;
import my.domain.deposit.DepositVO;
import my.domain.rental.RentalSettlementMapper;
import my.domain.rental.RentalSettlementVO;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {

    private final BookCaseOccupiedRecordMapper occupiedRecordMapper;
    private final RentalSettlementMapper rentalSettlementMapper;
    private final DepositMapper depositMapper;
    private final DepositRentalOffsetMapper depositRentalOffsetMapper;
    private final BookCaseService bookCaseService;



    @Override
    public void processMonthlyOverdue() {
        String currentMonth = YearMonth.now().toString();
        List<BookCaseOccupiedRecordVO> bcors = occupiedRecordMapper.selectAllActive();

        // BookOwner 단위로 묶기 (deposit이 BookOwner 1:1이므로)
        Map<Long, List<BookCaseOccupiedRecordVO>> byOwner = bcors.stream()
                .collect(Collectors.groupingBy(BookCaseOccupiedRecordVO::getBookOwnerId));

        for (Map.Entry<Long, List<BookCaseOccupiedRecordVO>> entry : byOwner.entrySet()) {
            try {
                processOwnerOverdue(entry.getKey(), entry.getValue(), currentMonth);
            } catch (Exception e) {
                log.warn("BookOwner {} 연체 처리 실패: {}", entry.getKey(), e.getMessage());
            }
        }
    }

    private void processOwnerOverdue(Long bookOwnerId, List<BookCaseOccupiedRecordVO> bcors, String currentMonth) {
        // 1. 이미 SUSPENDED인 점유가 있으면 강제 퇴거
        for (BookCaseOccupiedRecordVO bcor : bcors) {
            if (bcor.getSuspendedAt() != null) {
                log.warn("BookOwner {} 책장 {} 강제 퇴거 처리", bookOwnerId, bcor.getBookCaseId());
                bookCaseService.unOccupyProcess(List.of(bcor.getBookCaseId()));
            }
        }

        // 2. 전체 점유에서 연체 임대료 수집 (FIFO: TARGET_MONTH ASC)
        List<RentalSettlementVO> overdueList = new ArrayList<>();
        for (BookCaseOccupiedRecordVO bcor : bcors) {
            if (bcor.getSuspendedAt() != null) continue;
            overdueList.addAll(
                    rentalSettlementMapper.selectOverdueByOccupiedRecordId(bcor.getId(), currentMonth));
        }

        if (overdueList.isEmpty()) return;

        // 3. 보증금 조회
        DepositVO deposit = depositMapper.selectByBookOwnerId(bookOwnerId);
        if (deposit == null) {
            log.warn("BookOwner {} 보증금 없음", bookOwnerId);
            return;
        }

        // 4. FIFO 공제
        for (RentalSettlementVO rental : overdueList) {
            int depositRemain = deposit.getRemainingAmount();

            if (depositRemain <= 0) {
                // 보증금 소진 상태에서 추가 연체 → SUSPENDED 처리
                suspendOwnerRecords(bcors);
                break;
            }

            int rentalRemain = rental.getRemainingAmount();
            int offsetAmount = Math.min(depositRemain, rentalRemain);

            // 보증금 차감
            deposit.setRemainingAmount(depositRemain - offsetAmount);
            deposit.setStatus(deposit.getRemainingAmount() > 0 ? "HELD" : "DEPLETED");
            depositMapper.update(deposit);

            // 임대료 공제 반영
            rental.setDeductedAmount(rental.getDeductedAmount() + offsetAmount);
            rental.setRemainingAmount(rentalRemain - offsetAmount);
            if (rental.getRemainingAmount() == 0) {
                rental.setStatus("PAID");
            }
            rentalSettlementMapper.update(rental);

            // 상계 내역 기록
            DepositRentalOffsetVO offset = new DepositRentalOffsetVO();
            offset.setDepositId(deposit.getId());
            offset.setRentalSettlementId(rental.getId());
            offset.setOffsetAmount(offsetAmount);
            depositRentalOffsetMapper.insert(offset);

            log.info("보증금 공제: BookOwner {} | 임대료 {} | 공제액 {} | 보증금잔액 {}",
                    bookOwnerId, rental.getTargetMonth(), offsetAmount, deposit.getRemainingAmount());
        }
    }

    private void suspendOwnerRecords(List<BookCaseOccupiedRecordVO> bcors) {
        for (BookCaseOccupiedRecordVO bcor : bcors) {
            if (bcor.getSuspendedAt() == null) {
                occupiedRecordMapper.updateSuspendedAt(bcor.getId());
                log.warn("책장 {} 중지 처리 (보증금 소진 후 추가 연체)", bcor.getBookCaseId());
            }
        }
    }
}

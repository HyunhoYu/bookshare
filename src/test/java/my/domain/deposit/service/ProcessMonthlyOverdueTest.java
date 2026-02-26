package my.domain.deposit.service;

import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseOccupiedRecordMapper;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.service.BookCaseService;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.deposit.DepositMapper;
import my.domain.deposit.DepositRentalOffsetMapper;
import my.domain.deposit.DepositRentalOffsetVO;
import my.domain.deposit.DepositVO;
import my.domain.rental.RentalSettlementMapper;
import my.domain.rental.RentalSettlementVO;
import my.domain.settlement_ratio.service.SettlementRatioService;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProcessMonthlyOverdueTest {

    @Autowired private DepositService depositService;
    @Autowired private BookCaseService bookCaseService;
    @Autowired private BookCaseTypeService bookCaseTypeService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;
    @Autowired private SettlementRatioService settlementRatioService;
    @Autowired private DepositMapper depositMapper;
    @Autowired private DepositRentalOffsetMapper depositRentalOffsetMapper;
    @Autowired private RentalSettlementMapper rentalSettlementMapper;
    @Autowired private BookCaseOccupiedRecordMapper occupiedRecordMapper;

    private long typeId;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        String code = uniqueCode();
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(50000);
        typeId = bookCaseTypeService.create(typeDto);

        SettlementRatioVO ratioVO = new SettlementRatioVO();
        ratioVO.setOwnerRatio(0.7);
        ratioVO.setStoreRatio(0.3);
        settlementRatioService.create(ratioVO);
    }

    private long createBookCase() {
        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        return bookCaseService.create(caseDto);
    }

    private BookOwnerVO createBookOwner() {
        String code = uniqueCode();
        return bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("owner" + code)
                .email("dep-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
    }

    private String pastMonth(int monthsAgo) {
        return YearMonth.now().minusMonths(monthsAgo).toString();
    }

    /**
     * 점유 생성 + 보증금 생성.
     * occupy()가 생성하는 임대료는 현재월 이후이므로 연체가 아님.
     * 연체 테스트를 위한 과거 월 임대료는 insertOverdue()로 별도 생성.
     */
    private BookCaseOccupiedRecordVO occupyBookCase(BookOwnerVO owner, long bookCaseId, int depositAmount) {
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(6), depositAmount);
        return occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
    }

    /**
     * 과거 월 연체 임대료를 직접 INSERT. 금액을 정확히 제어 가능.
     */
    private RentalSettlementVO insertOverdue(long occupiedRecordId, Long bookOwnerId, String targetMonth, int amount) {
        RentalSettlementVO rs = new RentalSettlementVO();
        rs.setOccupiedRecordId(occupiedRecordId);
        rs.setBookOwnerId(bookOwnerId);
        rs.setTargetMonth(targetMonth);
        rs.setAmount(amount);
        rentalSettlementMapper.insert(rs);
        return rentalSettlementMapper.selectById(rs.getId());
    }

    @Nested
    @DisplayName("정상 케이스")
    class NormalCase {

        @Test
        @DisplayName("연체 없음 - 보증금 변동 없음")
        void noOverdue_noChange() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            occupyBookCase(owner, bookCaseId, 50000);

            DepositVO before = depositMapper.selectByBookOwnerId(owner.getId());

            depositService.processMonthlyOverdue();

            DepositVO after = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(after.getRemainingAmount()).isEqualTo(before.getRemainingAmount());
            assertThat(after.getStatus()).isEqualTo("HELD");

            List<DepositRentalOffsetVO> offsets = depositRentalOffsetMapper.selectByDepositId(after.getId());
            assertThat(offsets).isEmpty();
        }

        @Test
        @DisplayName("연체 1건 - 보증금으로 완전 공제")
        void singleOverdue_fullyDeducted() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            BookCaseOccupiedRecordVO record = occupyBookCase(owner, bookCaseId, 50000);

            insertOverdue(record.getId(), owner.getId(), pastMonth(1), 30000);

            depositService.processMonthlyOverdue();

            DepositVO deposit = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(deposit.getRemainingAmount()).isEqualTo(20000);
            assertThat(deposit.getStatus()).isEqualTo("HELD");

            // 연체 임대료가 PAID 처리됨
            List<RentalSettlementVO> overdue = rentalSettlementMapper.selectOverdueByOccupiedRecordId(
                    record.getId(), YearMonth.now().toString());
            assertThat(overdue).isEmpty();

            // 상계 내역 1건
            List<DepositRentalOffsetVO> offsets = depositRentalOffsetMapper.selectByDepositId(deposit.getId());
            assertThat(offsets).hasSize(1);
            assertThat(offsets.get(0).getOffsetAmount()).isEqualTo(30000);
        }

        @Test
        @DisplayName("연체 여러 건 - 보증금으로 전부 공제 (FIFO)")
        void multipleOverdue_allDeducted_fifo() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            BookCaseOccupiedRecordVO record = occupyBookCase(owner, bookCaseId, 100000);

            RentalSettlementVO older = insertOverdue(record.getId(), owner.getId(), pastMonth(2), 30000);
            RentalSettlementVO newer = insertOverdue(record.getId(), owner.getId(), pastMonth(1), 30000);

            depositService.processMonthlyOverdue();

            DepositVO deposit = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(deposit.getRemainingAmount()).isEqualTo(40000);
            assertThat(deposit.getStatus()).isEqualTo("HELD");

            List<DepositRentalOffsetVO> offsets = depositRentalOffsetMapper.selectByDepositId(deposit.getId());
            assertThat(offsets).hasSize(2);

            // FIFO: 첫 번째 오프셋은 더 오래된 연체(pastMonth(2))에 대한 공제
            assertThat(offsets.get(0).getRentalSettlementId()).isEqualTo(older.getId());
            assertThat(offsets.get(0).getOffsetAmount()).isEqualTo(30000);
            assertThat(offsets.get(1).getRentalSettlementId()).isEqualTo(newer.getId());
            assertThat(offsets.get(1).getOffsetAmount()).isEqualTo(30000);
        }

        @Test
        @DisplayName("보증금으로 부분 공제 - 임대료 1건을 다 못 갚음")
        void partialDeduction_insufficientDeposit() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            BookCaseOccupiedRecordVO record = occupyBookCase(owner, bookCaseId, 20000);

            RentalSettlementVO overdue = insertOverdue(record.getId(), owner.getId(), pastMonth(1), 50000);

            depositService.processMonthlyOverdue();

            DepositVO deposit = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(deposit.getRemainingAmount()).isEqualTo(0);
            assertThat(deposit.getStatus()).isEqualTo("DEPLETED");

            // 임대료: 부분 공제, 여전히 UNPAID
            RentalSettlementVO afterRental = rentalSettlementMapper.selectById(overdue.getId());
            assertThat(afterRental.getDeductedAmount()).isEqualTo(20000);
            assertThat(afterRental.getRemainingAmount()).isEqualTo(30000);
            assertThat(afterRental.getStatus()).isEqualTo("UNPAID");

            // 상계 내역 1건
            List<DepositRentalOffsetVO> offsets = depositRentalOffsetMapper.selectByDepositId(deposit.getId());
            assertThat(offsets).hasSize(1);
            assertThat(offsets.get(0).getOffsetAmount()).isEqualTo(20000);
        }

        @Test
        @DisplayName("보증금 일부 공제 후 소진 - 여러 건 중 중간에 소진되면 SUSPENDED")
        void depositDepleted_midway_suspended() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            BookCaseOccupiedRecordVO record = occupyBookCase(owner, bookCaseId, 40000);

            insertOverdue(record.getId(), owner.getId(), pastMonth(3), 30000);
            insertOverdue(record.getId(), owner.getId(), pastMonth(2), 30000);
            insertOverdue(record.getId(), owner.getId(), pastMonth(1), 30000);

            depositService.processMonthlyOverdue();

            DepositVO deposit = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(deposit.getRemainingAmount()).isEqualTo(0);
            assertThat(deposit.getStatus()).isEqualTo("DEPLETED");

            // 보증금 소진 후 추가 연체 → SUSPENDED 처리
            BookCaseOccupiedRecordVO afterRecord = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
            assertThat(afterRecord).isNotNull();
            assertThat(afterRecord.getSuspendedAt()).isNotNull();
        }

        @Test
        @DisplayName("보증금이 연체 금액과 정확히 일치 - DEPLETED되지만 SUSPENDED는 안 됨")
        void depositExactMatch_depletedButNotSuspended() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            BookCaseOccupiedRecordVO record = occupyBookCase(owner, bookCaseId, 50000);

            insertOverdue(record.getId(), owner.getId(), pastMonth(1), 50000);

            depositService.processMonthlyOverdue();

            DepositVO deposit = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(deposit.getRemainingAmount()).isEqualTo(0);
            assertThat(deposit.getStatus()).isEqualTo("DEPLETED");

            // 연체가 전부 공제되었으므로 SUSPENDED 안 됨
            BookCaseOccupiedRecordVO afterRecord = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
            assertThat(afterRecord).isNotNull();
            assertThat(afterRecord.getSuspendedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("중지/강제 퇴거 케이스")
    class SuspendAndEvictCase {

        @Test
        @DisplayName("보증금 소진 후 추가 연체 - SUSPENDED 처리")
        void depositDepleted_additionalOverdue_suspended() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            BookCaseOccupiedRecordVO record = occupyBookCase(owner, bookCaseId, 10000);

            insertOverdue(record.getId(), owner.getId(), pastMonth(2), 50000);
            insertOverdue(record.getId(), owner.getId(), pastMonth(1), 50000);

            depositService.processMonthlyOverdue();

            DepositVO deposit = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(deposit.getRemainingAmount()).isEqualTo(0);
            assertThat(deposit.getStatus()).isEqualTo("DEPLETED");

            // SUSPENDED 처리 확인
            BookCaseOccupiedRecordVO afterRecord = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
            assertThat(afterRecord).isNotNull();
            assertThat(afterRecord.getSuspendedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 SUSPENDED인 점유 - 강제 퇴거 (unOccupyProcess)")
        void alreadySuspended_forceEviction() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            occupyBookCase(owner, bookCaseId, 50000);

            // SUSPENDED 상태로 만들기
            BookCaseOccupiedRecordVO record = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
            occupiedRecordMapper.updateSuspendedAt(record.getId());

            depositService.processMonthlyOverdue();

            // 강제 퇴거 → 점유 해제됨
            BookCaseOccupiedRecordVO afterRecord = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
            assertThat(afterRecord).isNull();
            assertThat(bookCaseService.isOccupied(bookCaseId)).isFalse();
        }
    }

    @Nested
    @DisplayName("복합 케이스")
    class ComplexCase {

        @Test
        @DisplayName("한 BookOwner 책장 2개 - 보증금 1건으로 통합 공제")
        void twoBookCases_singleDeposit() {
            long bookCaseId1 = createBookCase();
            long bookCaseId2 = createBookCase();
            BookOwnerVO owner = createBookOwner();

            BookCaseOccupiedRecordVO rec1 = occupyBookCase(owner, bookCaseId1, 100000);
            BookCaseOccupiedRecordVO rec2 = occupyBookCase(owner, bookCaseId2, 50000);

            // 보증금 누적 적립 확인
            DepositVO depositBefore = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(depositBefore.getAmount()).isEqualTo(150000);

            // 각 책장에서 1건씩 연체 생성
            insertOverdue(rec1.getId(), owner.getId(), pastMonth(2), 30000);
            insertOverdue(rec2.getId(), owner.getId(), pastMonth(1), 30000);

            depositService.processMonthlyOverdue();

            DepositVO depositAfter = depositMapper.selectByBookOwnerId(owner.getId());
            assertThat(depositAfter.getRemainingAmount()).isEqualTo(90000);
            assertThat(depositAfter.getStatus()).isEqualTo("HELD");

            List<DepositRentalOffsetVO> offsets = depositRentalOffsetMapper.selectByDepositId(depositAfter.getId());
            assertThat(offsets).hasSize(2);
        }

        @Test
        @DisplayName("여러 BookOwner - 각각 독립 처리")
        void multipleOwners_independentProcessing() {
            // Owner A: 연체 있음
            long bookCaseIdA = createBookCase();
            BookOwnerVO ownerA = createBookOwner();
            BookCaseOccupiedRecordVO recA = occupyBookCase(ownerA, bookCaseIdA, 50000);
            insertOverdue(recA.getId(), ownerA.getId(), pastMonth(1), 30000);

            // Owner B: 연체 없음
            long bookCaseIdB = createBookCase();
            BookOwnerVO ownerB = createBookOwner();
            occupyBookCase(ownerB, bookCaseIdB, 50000);

            depositService.processMonthlyOverdue();

            // Owner A: 공제 처리됨
            DepositVO depositA = depositMapper.selectByBookOwnerId(ownerA.getId());
            assertThat(depositA.getRemainingAmount()).isEqualTo(20000);

            // Owner B: 보증금 변동 없음
            DepositVO depositB = depositMapper.selectByBookOwnerId(ownerB.getId());
            assertThat(depositB.getRemainingAmount()).isEqualTo(50000);

            List<DepositRentalOffsetVO> offsetsB = depositRentalOffsetMapper.selectByDepositId(depositB.getId());
            assertThat(offsetsB).isEmpty();
        }

        @Test
        @DisplayName("보증금이 없는 BookOwner - 에러 없이 스킵")
        void noDeposit_skipped() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();

            // 보증금 없이 점유 레코드만 직접 생성 (occupy 우회)
            BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
            record.setBookOwnerId(owner.getId());
            record.setBookCaseId(bookCaseId);
            record.setExpirationDate(LocalDate.now().plusMonths(6));
            occupiedRecordMapper.insert(record);

            // 연체 임대료 직접 생성
            RentalSettlementVO rental = insertOverdue(record.getId(), owner.getId(), pastMonth(1), 50000);

            // 보증금 없이 실행 → 에러 없이 스킵
            depositService.processMonthlyOverdue();

            // 임대료 상태 변동 없음
            RentalSettlementVO after = rentalSettlementMapper.selectById(rental.getId());
            assertThat(after.getStatus()).isEqualTo("UNPAID");
            assertThat(after.getRemainingAmount()).isEqualTo(50000);
        }
    }
}

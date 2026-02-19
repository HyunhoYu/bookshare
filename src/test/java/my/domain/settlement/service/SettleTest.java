package my.domain.settlement.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.service.BookCaseService;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.service.BookSoldRecordService;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.customer.service.auth.CustomerAuthService;
import my.domain.rental.RentalSettlementMapper;
import my.domain.rental.RentalSettlementVO;
import my.domain.settlement.SettlementRentalOffsetMapper;
import my.domain.settlement.dto.SettlementRequestDto;
import my.domain.settlement.vo.SettlementRentalOffsetVO;
import my.domain.settlement.vo.SettlementVO;
import my.domain.settlement_ratio.service.SettlementRatioService;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserJoinRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SettleTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private BookSoldRecordService bookSoldRecordService;

    @Autowired
    private BookSoldRecordMapper bookSoldRecordMapper;

    @Autowired
    private BookCaseService bookCaseService;

    @Autowired
    private BookCaseTypeService bookCaseTypeService;

    @Autowired
    private BookOwnerAuthService bookOwnerAuthService;

    @Autowired
    private CustomerAuthService customerAuthService;

    @Autowired
    private SettlementRatioService settlementRatioService;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private RentalSettlementMapper rentalSettlementMapper;

    @Autowired
    private SettlementRentalOffsetMapper settlementRentalOffsetMapper;

    private Long customerId;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        // 정산 비율 설정
        SettlementRatioVO ratioVO = new SettlementRatioVO();
        ratioVO.setOwnerRatio(0.7);
        ratioVO.setStoreRatio(0.3);
        settlementRatioService.create(ratioVO);

        // 고객 생성
        String custCode = uniqueCode();
        UserVO customer = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("테스트고객")
                .email("settle-cust-" + custCode + "@test.com")
                .phone("010-" + custCode.substring(0, 4) + "-" + custCode.substring(4))
                .password("password123")
                .residentNumber(custCode + "-2345678")
                .build());
        customerId = customer.getId();
    }

    /**
     * BookOwner 생성 + 책장 생성/점유 + 책 등록 + 판매까지 수행하는 헬퍼
     * 반환: BookOwnerVO
     */
    private BookOwnerVO createOwnerAndSellBook(String emailPrefix, String bookName, int price) {
        String code = uniqueCode();

        // 책장 타입 생성
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        // 책장 생성
        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        // BookOwner 생성
        String ownerName = "오너" + code;
        String ownerPhone = "010-" + code.substring(0, 4) + "-" + code.substring(4);
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName)
                .email(emailPrefix + "-" + code + "@test.com")
                .phone(ownerPhone)
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));

        // 책 등록
        BookRegisterDto bookDto = new BookRegisterDto();
        bookDto.setUserName(ownerName);
        bookDto.setUserPhone(ownerPhone);
        bookDto.setBookName(bookName);
        bookDto.setPublisherHouse("출판사");
        bookDto.setPrice(price);
        bookDto.setBookTypeCode("04");
        bookCaseService.registerBooks(bookCaseId, List.of(bookDto));

        // 판매
        Long bookId = getBookIdByOwner(owner);
        BuyBookRequestDto buyDto = new BuyBookRequestDto();
        buyDto.setBookId(bookId);
        buyDto.setCustomerId(customerId);
        buyDto.setBuyTypeCommonCode("01");
        bookSoldRecordService.sellBooks(List.of(buyDto));

        return owner;
    }

    /**
     * BookOwner 생성 + 책장 생성/점유 + 책 여러 권 등록 + 전부 판매하는 헬퍼
     * 반환: BookOwnerVO
     */
    private BookOwnerVO createOwnerAndSellMultipleBooks(String emailPrefix, List<String> bookNames, List<Integer> prices) {
        String code = uniqueCode();

        // 책장 타입 생성
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        // 책장 생성
        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        // BookOwner 생성
        String ownerName = "오너" + code;
        String ownerPhone = "010-" + code.substring(0, 4) + "-" + code.substring(4);
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName)
                .email(emailPrefix + "-" + code + "@test.com")
                .phone(ownerPhone)
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));

        // 책 여러 권 등록
        for (int i = 0; i < bookNames.size(); i++) {
            BookRegisterDto bookDto = new BookRegisterDto();
            bookDto.setUserName(ownerName);
            bookDto.setUserPhone(ownerPhone);
            bookDto.setBookName(bookNames.get(i));
            bookDto.setPublisherHouse("출판사");
            bookDto.setPrice(prices.get(i));
            bookDto.setBookTypeCode("04");
            bookCaseService.registerBooks(bookCaseId, List.of(bookDto));
        }

        // 전부 판매
        List<BookVO> books = bookMapper.selectBooksByBookOwnerId(owner.getId());
        for (BookVO book : books) {
            BuyBookRequestDto buyDto = new BuyBookRequestDto();
            buyDto.setBookId(book.getId());
            buyDto.setCustomerId(customerId);
            buyDto.setBuyTypeCommonCode("01");
            bookSoldRecordService.sellBooks(List.of(buyDto));
        }

        return owner;
    }

    private Long getBookIdByOwner(BookOwnerVO owner) {
        List<BookVO> books = bookMapper.selectBooksByBookOwnerId(owner.getId());
        return books.get(0).getId();
    }

    private List<Long> getSoldBookIdsByOwner(BookOwnerVO owner) {
        List<BookVO> books = bookMapper.selectSoldBookByBookOwnerId(owner.getId());
        return books.stream().map(BookVO::getId).toList();
    }

    private SettlementRequestDto createSettleRequest(Long bookOwnerId, List<Long> saleRecordIds) {
        SettlementRequestDto dto = new SettlementRequestDto();
        dto.setBookOwnerId(bookOwnerId);
        dto.setSaleRecordIds(saleRecordIds);
        return dto;
    }

    // ========== 성공 케이스 ==========

    @Test
    @DisplayName("정산 성공 - 단건 판매기록 정산")
    void settle_single_success() {
        BookOwnerVO owner = createOwnerAndSellBook("settle-ok", "이펙티브 자바", 36000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        SettlementVO result = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getBookOwnerId()).isEqualTo(owner.getId());
        assertThat(result.getSettledAt()).isNotNull();
    }

    @Test
    @DisplayName("정산 성공 - 다건(배치) 판매기록 정산")
    void settle_batch_success() {
        BookOwnerVO owner = createOwnerAndSellMultipleBooks("settle-batch",
                List.of("자바의 정석", "스프링 인 액션", "클린 코드"),
                List.of(30000, 40000, 25000));
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        SettlementVO result = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getBookOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    @DisplayName("정산 후 판매기록의 settlement_id가 업데이트됨")
    void settle_saleRecords_settlementId_updated() {
        BookOwnerVO owner = createOwnerAndSellBook("settle-upd", "리팩터링", 35000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        SettlementVO settlement = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        for (Long saleRecordId : saleRecordIds) {
            BookSoldRecordVO record = bookSoldRecordMapper.selectById(saleRecordId);
            assertThat(record.getBookOwnerSettlementId()).isEqualTo(settlement.getId());
        }
    }

    @Test
    @DisplayName("정산 후 미정산 내역에서 제외됨")
    void settle_removedFromUnsettled() {
        int baselineCount = settlementService.findAllUnsettled().size();

        BookOwnerVO owner = createOwnerAndSellBook("settle-rm", "디자인 패턴", 45000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        // 정산 전 미정산 1건 증가
        assertThat(settlementService.findAllUnsettled()).hasSize(baselineCount + 1);

        settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        // 정산 후 원래 건수로 복귀
        assertThat(settlementService.findAllUnsettled()).hasSize(baselineCount);
    }

    @Test
    @DisplayName("배치 정산 후 모든 판매기록이 동일한 settlement_id를 가짐")
    void settle_batch_allRecords_sameSettlementId() {
        BookOwnerVO owner = createOwnerAndSellMultipleBooks("settle-same",
                List.of("책1", "책2", "책3"),
                List.of(10000, 20000, 30000));
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        SettlementVO settlement = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        for (Long saleRecordId : saleRecordIds) {
            BookSoldRecordVO record = bookSoldRecordMapper.selectById(saleRecordId);
            assertThat(record.getBookOwnerSettlementId()).isEqualTo(settlement.getId());
        }
    }

    // ========== 실패 케이스 ==========

    @Test
    @DisplayName("실패 - saleRecordIds가 null")
    void settle_fail_nullSaleRecordIds() {
        SettlementRequestDto dto = createSettleRequest(1L, null);

        assertThatThrownBy(() -> settlementService.settle(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.EMPTY_SETTLEMENT_REQUEST.getMessage());
    }

    @Test
    @DisplayName("실패 - saleRecordIds가 빈 리스트")
    void settle_fail_emptySaleRecordIds() {
        SettlementRequestDto dto = createSettleRequest(1L, List.of());

        assertThatThrownBy(() -> settlementService.settle(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.EMPTY_SETTLEMENT_REQUEST.getMessage());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 bookOwner")
    void settle_fail_bookOwnerNotFound() {
        SettlementRequestDto dto = createSettleRequest(999999L, List.of(1L));

        assertThatThrownBy(() -> settlementService.settle(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.BOOK_OWNER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 다른 bookOwner의 판매기록으로 정산 시도")
    void settle_fail_saleRecordOwnerMismatch() {
        BookOwnerVO owner1 = createOwnerAndSellBook("settle-o1", "자바의 정석", 30000);
        BookOwnerVO owner2 = createOwnerAndSellBook("settle-o2", "스프링 인 액션", 40000);

        // owner1의 판매기록을 owner2가 정산 시도
        List<Long> owner1SaleIds = getSoldBookIdsByOwner(owner1);
        SettlementRequestDto dto = createSettleRequest(owner2.getId(), owner1SaleIds);

        assertThatThrownBy(() -> settlementService.settle(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.SETTLEMENT_SALE_RECORD_OWNER_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("실패 - 이미 정산된 판매기록으로 재정산 시도")
    void settle_fail_alreadySettled() {
        BookOwnerVO owner = createOwnerAndSellBook("settle-dup", "이펙티브 자바", 36000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        // 1차 정산 성공
        settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        // 2차 정산 시도 - 실패
        assertThatThrownBy(() -> settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds)))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.SETTLEMENT_ALREADY_SETTLED.getMessage());
    }

    @Test
    @DisplayName("실패 - 일부만 해당 bookOwner 소유인 경우 (소유자 불일치)")
    void settle_fail_partialOwnerMismatch() {
        BookOwnerVO owner1 = createOwnerAndSellBook("settle-p1", "자바의 정석", 30000);
        BookOwnerVO owner2 = createOwnerAndSellBook("settle-p2", "스프링 인 액션", 40000);

        List<Long> owner1SaleIds = getSoldBookIdsByOwner(owner1);
        List<Long> owner2SaleIds = getSoldBookIdsByOwner(owner2);

        // owner1의 판매기록 + owner2의 판매기록을 섞어서 owner1으로 정산 시도
        List<Long> mixedIds = List.of(owner1SaleIds.get(0), owner2SaleIds.get(0));
        SettlementRequestDto dto = createSettleRequest(owner1.getId(), mixedIds);

        assertThatThrownBy(() -> settlementService.settle(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.SETTLEMENT_SALE_RECORD_OWNER_MISMATCH.getMessage());
    }

    // ========== 상계(공제) 테스트 케이스 ==========

    @Test
    @DisplayName("상계 - 미납 임대료 없으면 기존과 동일 (회귀 테스트)")
    void settle_no_unpaid_rental() {
        // 점유 시 임대료가 생성되지만, 전부 PAID로 처리한 후 정산
        BookOwnerVO owner = createOwnerAndSellBook("settle-norental", "클린 코드", 50000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        // 미납 임대료를 전부 납부 처리
        List<RentalSettlementVO> unpaidRentals = rentalSettlementMapper.selectUnpaidByBookOwnerId(owner.getId());
        for (RentalSettlementVO rental : unpaidRentals) {
            rentalSettlementMapper.updateStatusPaid(rental.getId());
        }

        SettlementVO result = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        assertThat(result.getDeductedRentalAmount()).isEqualTo(0);
        assertThat(result.getActualPayoutAmount()).isEqualTo(result.getOwnerAmount());
    }

    @Test
    @DisplayName("상계 - 미납 < 정산금: 전액 공제 + 차액 송금")
    void settle_with_rental_offset_full() {
        // 책 가격 200000원 → ownerAmount = 200000 * 0.7 = 140000원
        // 임대료 월 50000원 × 약 3~4건 = 미납 총액이 정산금보다 큼/작을 수 있음
        // 미납 임대료를 1건만 남기고 나머지 납부 처리
        BookOwnerVO owner = createOwnerAndSellBook("settle-offset1", "자바 완벽 가이드", 200000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        // 미납 임대료 중 1건만 남기고 납부 처리 (50000원 1건만 미납)
        List<RentalSettlementVO> unpaidRentals = rentalSettlementMapper.selectUnpaidByBookOwnerId(owner.getId());
        for (int i = 1; i < unpaidRentals.size(); i++) {
            rentalSettlementMapper.updateStatusPaid(unpaidRentals.get(i).getId());
        }

        // ownerAmount = 140000, 미납 임대료 = 50000 (1건)
        SettlementVO result = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        assertThat(result.getDeductedRentalAmount()).isEqualTo(unpaidRentals.get(0).getAmount());
        assertThat(result.getActualPayoutAmount()).isEqualTo(result.getOwnerAmount() - result.getDeductedRentalAmount());
        assertThat(result.getActualPayoutAmount()).isGreaterThan(0);

        // 공제된 임대료가 PAID로 전환됨
        RentalSettlementVO updated = rentalSettlementMapper.selectById(unpaidRentals.get(0).getId());
        assertThat(updated.getStatus()).isEqualTo("PAID");
        assertThat(updated.getRemainingAmount()).isEqualTo(0);
        assertThat(updated.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("상계 - 미납 > 정산금: 한도까지만 공제, 송금 0")
    void settle_with_rental_offset_exceeds() {
        // 책 가격 10000원 → ownerAmount = 7000원
        // 임대료 월 50000원 × 3~4건 → 미납 총액이 7000원보다 훨씬 큼
        BookOwnerVO owner = createOwnerAndSellBook("settle-offset2", "작은 책", 10000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        SettlementVO result = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        // ownerAmount(7000)가 미납(50000*n)보다 작으므로 전액 상계
        assertThat(result.getDeductedRentalAmount()).isEqualTo(result.getOwnerAmount());
        assertThat(result.getActualPayoutAmount()).isEqualTo(0);
        assertThat(result.getPayoutKey()).isEqualTo("RENTAL_OFFSET");
        assertThat(result.getTransferStatus()).isEqualTo("OFFSET_COMPLETED");
    }

    @Test
    @DisplayName("상계 - 부분 공제: 마지막 건 일부만 공제, remainingAmount 검증")
    void settle_with_partial_deduction() {
        // 책 가격 100000원 → ownerAmount = 70000원
        // 미납 임대료 2건 유지 (첫 달은 일할 계산일 수 있음)
        BookOwnerVO owner = createOwnerAndSellBook("settle-partial", "중간 책", 100000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        // 미납 2건만 남기고 나머지 납부
        List<RentalSettlementVO> unpaidRentals = rentalSettlementMapper.selectUnpaidByBookOwnerId(owner.getId());
        for (int i = 2; i < unpaidRentals.size(); i++) {
            rentalSettlementMapper.updateStatusPaid(unpaidRentals.get(i).getId());
        }

        int rental1Amount = unpaidRentals.get(0).getAmount();
        int rental2Amount = unpaidRentals.get(1).getAmount();
        int ownerAmount = 70000; // 100000 * 0.7

        SettlementVO result = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        // ownerAmount(70000) vs 미납 합계 비교
        int totalUnpaid = rental1Amount + rental2Amount;
        int expectedDeducted = Math.min(ownerAmount, totalUnpaid);
        assertThat(result.getDeductedRentalAmount()).isEqualTo(expectedDeducted);
        assertThat(result.getActualPayoutAmount()).isEqualTo(ownerAmount - expectedDeducted);

        // 첫 번째 임대료: 전액 공제 → PAID
        RentalSettlementVO first = rentalSettlementMapper.selectById(unpaidRentals.get(0).getId());
        assertThat(first.getStatus()).isEqualTo("PAID");
        assertThat(first.getRemainingAmount()).isEqualTo(0);

        // 두 번째 임대료: 부분 공제
        RentalSettlementVO second = rentalSettlementMapper.selectById(unpaidRentals.get(1).getId());
        int expectedSecondDeducted = expectedDeducted - rental1Amount;
        assertThat(second.getDeductedAmount()).isEqualTo(expectedSecondDeducted);
        assertThat(second.getRemainingAmount()).isEqualTo(rental2Amount - expectedSecondDeducted);
        if (second.getRemainingAmount() == 0) {
            assertThat(second.getStatus()).isEqualTo("PAID");
        } else {
            assertThat(second.getStatus()).isEqualTo("UNPAID");
        }
    }

    @Test
    @DisplayName("상계 - 전액 공제된 임대료 STATUS=PAID + PAID_AT 확인")
    void settle_rental_paid_after_full_deduction() {
        BookOwnerVO owner = createOwnerAndSellBook("settle-paidat", "큰 책", 200000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        // 1건만 미납 유지
        List<RentalSettlementVO> unpaidRentals = rentalSettlementMapper.selectUnpaidByBookOwnerId(owner.getId());
        for (int i = 1; i < unpaidRentals.size(); i++) {
            rentalSettlementMapper.updateStatusPaid(unpaidRentals.get(i).getId());
        }

        settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        RentalSettlementVO updated = rentalSettlementMapper.selectById(unpaidRentals.get(0).getId());
        assertThat(updated.getStatus()).isEqualTo("PAID");
        assertThat(updated.getPaidAt()).isNotNull();
        assertThat(updated.getDeductedAmount()).isEqualTo(updated.getAmount());
        assertThat(updated.getRemainingAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("상계 - OFFSET 레코드가 올바르게 생성됨")
    void settle_offset_records_created() {
        // 책 가격 200000원 → ownerAmount = 140000원
        // 미납 2건 유지 (첫 달은 일할 계산일 수 있음)
        BookOwnerVO owner = createOwnerAndSellBook("settle-offrecord", "큰 도서", 200000);
        List<Long> saleRecordIds = getSoldBookIdsByOwner(owner);

        List<RentalSettlementVO> unpaidRentals = rentalSettlementMapper.selectUnpaidByBookOwnerId(owner.getId());
        // 2건만 남기고 나머지 납부
        for (int i = 2; i < unpaidRentals.size(); i++) {
            rentalSettlementMapper.updateStatusPaid(unpaidRentals.get(i).getId());
        }

        int rental1Amount = unpaidRentals.get(0).getAmount();
        int rental2Amount = unpaidRentals.get(1).getAmount();

        SettlementVO result = settlementService.settle(createSettleRequest(owner.getId(), saleRecordIds));

        List<SettlementRentalOffsetVO> offsets = settlementRentalOffsetMapper.selectBySettlementId(result.getId());
        assertThat(offsets).hasSize(2);

        // 첫 번째 OFFSET: 전액 공제
        SettlementRentalOffsetVO offset1 = offsets.stream()
                .filter(o -> o.getRentalSettlementId().equals(unpaidRentals.get(0).getId()))
                .findFirst().orElseThrow();
        assertThat(offset1.getOffsetAmount()).isEqualTo(rental1Amount);
        assertThat(offset1.getSettlementId()).isEqualTo(result.getId());

        // 두 번째 OFFSET: 전액 공제 (ownerAmount 140000 > rental1 + rental2)
        SettlementRentalOffsetVO offset2 = offsets.stream()
                .filter(o -> o.getRentalSettlementId().equals(unpaidRentals.get(1).getId()))
                .findFirst().orElseThrow();
        assertThat(offset2.getOffsetAmount()).isEqualTo(rental2Amount);
    }

    @Test
    @DisplayName("상계 - 같은 임대료가 2번 정산에 걸쳐 부분 공제")
    void settle_multiple_settlements_same_rental() {
        // 높은 월 임대료(200000) + 소액 책(5000원, ownerAmount=3500)으로
        // 첫 정산에서 부분 공제만 되도록 설정

        String code = uniqueCode();
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(200000);
        long typeId = bookCaseTypeService.create(typeDto);

        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        String ownerName = "오너" + code;
        String ownerPhone = "010-" + code.substring(0, 4) + "-" + code.substring(4);
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName)
                .email("settle-multi-" + code + "@test.com")
                .phone(ownerPhone)
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));

        // 책 2권 등록 (소액)
        BookRegisterDto bookDto1 = new BookRegisterDto();
        bookDto1.setUserName(ownerName);
        bookDto1.setUserPhone(ownerPhone);
        bookDto1.setBookName("첫 번째 책");
        bookDto1.setPublisherHouse("출판사");
        bookDto1.setPrice(5000);
        bookDto1.setBookTypeCode("04");
        bookCaseService.registerBooks(bookCaseId, List.of(bookDto1));

        BookRegisterDto bookDto2 = new BookRegisterDto();
        bookDto2.setUserName(ownerName);
        bookDto2.setUserPhone(ownerPhone);
        bookDto2.setBookName("두 번째 책");
        bookDto2.setPublisherHouse("출판사");
        bookDto2.setPrice(5000);
        bookDto2.setBookTypeCode("04");
        bookCaseService.registerBooks(bookCaseId, List.of(bookDto2));

        List<BookVO> books = bookMapper.selectBooksByBookOwnerId(owner.getId());

        // 첫 번째 책만 판매
        BuyBookRequestDto buyDto1 = new BuyBookRequestDto();
        buyDto1.setBookId(books.get(0).getId());
        buyDto1.setCustomerId(customerId);
        buyDto1.setBuyTypeCommonCode("01");
        bookSoldRecordService.sellBooks(List.of(buyDto1));

        // 미납 1건만 남기고 나머지 납부 (일할 계산 포함 가장 큰 임대료 = 두 번째 것, 즉 정상월 50000이상)
        List<RentalSettlementVO> unpaidRentals = rentalSettlementMapper.selectUnpaidByBookOwnerId(owner.getId());
        // 두 번째 것(정상월 200000)만 남기고 나머지 납부
        for (int i = 0; i < unpaidRentals.size(); i++) {
            if (i != 1) {
                rentalSettlementMapper.updateStatusPaid(unpaidRentals.get(i).getId());
            }
        }
        Long targetRentalId = unpaidRentals.get(1).getId();
        int targetRentalAmount = unpaidRentals.get(1).getAmount(); // 200000

        // 1차 정산: ownerAmount = 3500 (5000 * 0.7) → 200000 중 3500만 공제
        int ownerAmount1 = 3500;
        List<Long> saleIds1 = List.of(books.get(0).getId());
        SettlementVO result1 = settlementService.settle(createSettleRequest(owner.getId(), saleIds1));
        assertThat(result1.getDeductedRentalAmount()).isEqualTo(ownerAmount1);

        RentalSettlementVO afterFirst = rentalSettlementMapper.selectById(targetRentalId);
        assertThat(afterFirst.getStatus()).isEqualTo("UNPAID");
        assertThat(afterFirst.getRemainingAmount()).isEqualTo(targetRentalAmount - ownerAmount1);

        // 두 번째 책 판매
        BuyBookRequestDto buyDto2 = new BuyBookRequestDto();
        buyDto2.setBookId(books.get(1).getId());
        buyDto2.setCustomerId(customerId);
        buyDto2.setBuyTypeCommonCode("01");
        bookSoldRecordService.sellBooks(List.of(buyDto2));

        // 2차 정산: ownerAmount = 3500 → 잔여 196500 중 3500 공제
        int ownerAmount2 = 3500;
        List<Long> saleIds2 = List.of(books.get(1).getId());
        SettlementVO result2 = settlementService.settle(createSettleRequest(owner.getId(), saleIds2));
        assertThat(result2.getDeductedRentalAmount()).isEqualTo(ownerAmount2);
        assertThat(result2.getActualPayoutAmount()).isEqualTo(0);

        RentalSettlementVO afterSecond = rentalSettlementMapper.selectById(targetRentalId);
        // 아직 미납 (200000 - 3500 - 3500 = 193000)
        assertThat(afterSecond.getStatus()).isEqualTo("UNPAID");
        assertThat(afterSecond.getRemainingAmount()).isEqualTo(targetRentalAmount - ownerAmount1 - ownerAmount2);

        // OFFSET 레코드 확인: 해당 임대료에 2개의 OFFSET이 있어야 함
        List<SettlementRentalOffsetVO> offsets = settlementRentalOffsetMapper.selectByRentalSettlementId(targetRentalId);
        assertThat(offsets).hasSize(2);
        int totalOffset = offsets.stream().mapToInt(SettlementRentalOffsetVO::getOffsetAmount).sum();
        assertThat(totalOffset).isEqualTo(ownerAmount1 + ownerAmount2);
    }

    @Test
    @DisplayName("배치 정산 - settleAll()로 여러 BookOwner 일괄 정산")
    void settleAll_batch_success() {
        BookOwnerVO owner1 = createOwnerAndSellBook("batch-o1", "자바의 정석", 30000);
        BookOwnerVO owner2 = createOwnerAndSellBook("batch-o2", "스프링 인 액션", 40000);

        List<SettlementVO> results = settlementService.settleAll();

        // 최소 2명의 BookOwner가 정산됨
        assertThat(results.size()).isGreaterThanOrEqualTo(2);

        // owner1, owner2 모두 정산됨
        boolean owner1Settled = results.stream().anyMatch(r -> r.getBookOwnerId().equals(owner1.getId()));
        boolean owner2Settled = results.stream().anyMatch(r -> r.getBookOwnerId().equals(owner2.getId()));
        assertThat(owner1Settled).isTrue();
        assertThat(owner2Settled).isTrue();

        // 미정산 내역에서 제외됨
        List<BookSoldRecordVO> unsettled1 = bookSoldRecordMapper.selectUnsettledByBookOwnerId(owner1.getId());
        List<BookSoldRecordVO> unsettled2 = bookSoldRecordMapper.selectUnsettledByBookOwnerId(owner2.getId());
        assertThat(unsettled1).isEmpty();
        assertThat(unsettled2).isEmpty();
    }
}

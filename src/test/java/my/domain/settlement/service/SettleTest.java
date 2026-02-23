package my.domain.settlement.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcase.BookCaseCreateDto;
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
import my.domain.settlement.dto.SettlementRequestDto;
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

    // ========== 배치 정산 ==========

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

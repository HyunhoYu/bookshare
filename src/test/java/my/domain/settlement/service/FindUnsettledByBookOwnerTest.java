package my.domain.settlement.service;

import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.service.BookCaseService;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.BookOwnerService;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.service.BookSoldRecordService;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.customer.service.auth.CustomerAuthService;
import my.domain.settlement.dto.SettlementRequestDto;
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

@SpringBootTest
@Transactional
class FindUnsettledByBookOwnerTest {

    @Autowired private SettlementService settlementService;
    @Autowired private BookOwnerService bookOwnerService;
    @Autowired private BookSoldRecordService bookSoldRecordService;
    @Autowired private BookCaseService bookCaseService;
    @Autowired private BookCaseTypeService bookCaseTypeService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;
    @Autowired private CustomerAuthService customerAuthService;
    @Autowired private SettlementRatioService settlementRatioService;
    @Autowired private BookMapper bookMapper;

    private Long customerId;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        SettlementRatioVO ratioVO = new SettlementRatioVO();
        ratioVO.setOwnerRatio(0.7);
        ratioVO.setStoreRatio(0.3);
        settlementRatioService.create(ratioVO);

        String custCode = uniqueCode();
        UserVO customer = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("testcust")
                .email("unsettled-bo-" + custCode + "@test.com")
                .phone("010-" + custCode.substring(0, 4) + "-" + custCode.substring(4))
                .password("password123")
                .residentNumber(custCode + "-2345678")
                .build());
        customerId = customer.getId();
    }

    private BookOwnerVO createOwner(String prefix) {
        String code = uniqueCode();
        String ownerName = "owner" + code;
        String ownerPhone = "010-" + code.substring(0, 4) + "-" + code.substring(4);

        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName)
                .email(prefix + "-" + code + "@test.com")
                .phone(ownerPhone)
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3), 50000);

        return owner;
    }

    private Long registerAndSellBook(BookOwnerVO owner, String bookName, int price) {
        List<BookVO> books = bookMapper.selectBooksByBookOwnerId(owner.getId());
        // 책장 찾기: 점유 중인 책장의 ID
        // owner가 점유한 책장에서 기존 책 목록으로 bookCaseId 추출
        // 새 책을 등록하려면 bookCaseId 필요 -> owner의 기존 책에서 가져오거나, 별도로 조회
        // 간단하게: owner가 하나의 책장만 점유하므로, 해당 책장에 등록

        // owner 정보로 책장 찾기
        String ownerName = null;
        String ownerPhone = null;
        // UserMapper에서 조회하는 대신 BookOwnerVO에서 가져올 수 있지만,
        // BookOwnerVO가 UserVO를 extend하므로 name, phone 접근 가능
        ownerName = owner.getName();
        ownerPhone = owner.getPhone();

        // owner의 책에서 bookCaseId 가져오기 (이미 책이 있는 경우)
        Long bookCaseId;
        if (!books.isEmpty()) {
            bookCaseId = books.get(0).getBookCaseId();
        } else {
            // 아직 책이 없으면 occupied record에서 찾아야 하지만,
            // 테스트 편의상 첫 번째 책 등록 시에는 별도 헬퍼 사용
            // 이 경우는 없을 것 (createOwnerAndRegisterBooks 사용)
            throw new IllegalStateException("No books found for owner");
        }

        BookRegisterDto bookDto = new BookRegisterDto();
        bookDto.setUserName(ownerName);
        bookDto.setUserPhone(ownerPhone);
        bookDto.setBookName(bookName);
        bookDto.setPublisherHouse("publisher");
        bookDto.setPrice(price);
        bookDto.setBookTypeCode("04");
        List<BookVO> registered = bookCaseService.registerBooks(bookCaseId, List.of(bookDto));
        Long bookId = registered.get(0).getId();

        BuyBookRequestDto buyDto = new BuyBookRequestDto();
        buyDto.setBookId(bookId);
        buyDto.setCustomerId(customerId);
        buyDto.setBuyTypeCommonCode("01");
        bookSoldRecordService.sellBooks(List.of(buyDto));

        return bookId;
    }

    /**
     * Owner 생성 + 책 1권 등록 + 판매까지 한번에 수행
     */
    private OwnerWithSoldBook createOwnerAndSellOneBook(String prefix, String bookName, int price) {
        String code = uniqueCode();
        String ownerName = "owner" + code;
        String ownerPhone = "010-" + code.substring(0, 4) + "-" + code.substring(4);

        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName)
                .email(prefix + "-" + code + "@test.com")
                .phone(ownerPhone)
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3), 50000);

        BookRegisterDto bookDto = new BookRegisterDto();
        bookDto.setUserName(ownerName);
        bookDto.setUserPhone(ownerPhone);
        bookDto.setBookName(bookName);
        bookDto.setPublisherHouse("publisher");
        bookDto.setPrice(price);
        bookDto.setBookTypeCode("04");
        List<BookVO> registered = bookCaseService.registerBooks(bookCaseId, List.of(bookDto));
        Long bookId = registered.get(0).getId();

        BuyBookRequestDto buyDto = new BuyBookRequestDto();
        buyDto.setBookId(bookId);
        buyDto.setCustomerId(customerId);
        buyDto.setBuyTypeCommonCode("01");
        bookSoldRecordService.sellBooks(List.of(buyDto));

        return new OwnerWithSoldBook(owner, bookCaseId, bookId);
    }

    /**
     * Owner + 책장 + 여러 권 등록/판매
     */
    private OwnerWithSoldBooks createOwnerAndSellMultipleBooks(String prefix,
                                                                List<String> bookNames,
                                                                List<Integer> prices) {
        String code = uniqueCode();
        String ownerName = "owner" + code;
        String ownerPhone = "010-" + code.substring(0, 4) + "-" + code.substring(4);

        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName)
                .email(prefix + "-" + code + "@test.com")
                .phone(ownerPhone)
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3), 50000);

        List<Long> soldBookIds = new java.util.ArrayList<>();
        for (int i = 0; i < bookNames.size(); i++) {
            BookRegisterDto bookDto = new BookRegisterDto();
            bookDto.setUserName(ownerName);
            bookDto.setUserPhone(ownerPhone);
            bookDto.setBookName(bookNames.get(i));
            bookDto.setPublisherHouse("publisher");
            bookDto.setPrice(prices.get(i));
            bookDto.setBookTypeCode("04");
            List<BookVO> registered = bookCaseService.registerBooks(bookCaseId, List.of(bookDto));
            Long bookId = registered.get(0).getId();

            BuyBookRequestDto buyDto = new BuyBookRequestDto();
            buyDto.setBookId(bookId);
            buyDto.setCustomerId(customerId);
            buyDto.setBuyTypeCommonCode("01");
            bookSoldRecordService.sellBooks(List.of(buyDto));

            soldBookIds.add(bookId);
        }

        return new OwnerWithSoldBooks(owner, soldBookIds);
    }

    private SettlementRequestDto settleRequest(Long bookOwnerId, List<Long> saleRecordIds) {
        SettlementRequestDto dto = new SettlementRequestDto();
        dto.setBookOwnerId(bookOwnerId);
        dto.setSaleRecordIds(saleRecordIds);
        return dto;
    }

    // ===== 내부 헬퍼 클래스 =====
    record OwnerWithSoldBook(BookOwnerVO owner, Long bookCaseId, Long soldBookId) {}
    record OwnerWithSoldBooks(BookOwnerVO owner, List<Long> soldBookIds) {}

    // ===== 테스트 케이스 =====

    @Test
    @DisplayName("판매 기록이 없으면 빈 리스트 반환")
    void findUnSettled_noSales_returnsEmpty() {
        BookOwnerVO owner = createOwner("empty");

        List<BookSoldRecordVO> result = settlementService.findUnSettled(owner.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("책 1건 판매 후 미정산 1건 조회")
    void findUnSettled_oneSale_returnsOne() {
        OwnerWithSoldBook data = createOwnerAndSellOneBook("one", "이펙티브 자바", 36000);

        List<BookSoldRecordVO> result = settlementService.findUnSettled(data.owner().getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(data.soldBookId());
        assertThat(result.get(0).getSoldPrice()).isEqualTo(36000);
        assertThat(result.get(0).getBookOwnerSettlementId()).isNull();
    }

    @Test
    @DisplayName("여러 건 판매 후 전부 미정산으로 조회")
    void findUnSettled_multipleSales_returnsAll() {
        OwnerWithSoldBooks data = createOwnerAndSellMultipleBooks("multi",
                List.of("자바의 정석", "스프링 인 액션", "클린 코드"),
                List.of(30000, 40000, 25000));

        List<BookSoldRecordVO> result = settlementService.findUnSettled(data.owner().getId());

        assertThat(result).hasSize(3);
        assertThat(result).extracting(BookSoldRecordVO::getId)
                .containsExactlyInAnyOrderElementsOf(data.soldBookIds());
    }

    @Test
    @DisplayName("다른 책소유주의 판매 기록은 조회되지 않음")
    void findUnSettled_otherOwnerExcluded() {
        OwnerWithSoldBook owner1Data = createOwnerAndSellOneBook("iso1", "자바의 정석", 30000);
        OwnerWithSoldBook owner2Data = createOwnerAndSellOneBook("iso2", "스프링 인 액션", 40000);

        List<BookSoldRecordVO> result1 = settlementService.findUnSettled(owner1Data.owner().getId());
        List<BookSoldRecordVO> result2 = settlementService.findUnSettled(owner2Data.owner().getId());

        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getId()).isEqualTo(owner1Data.soldBookId());

        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getId()).isEqualTo(owner2Data.soldBookId());
    }

    @Test
    @DisplayName("정산 완료된 기록은 미정산 조회에서 제외됨")
    void findUnSettled_settledExcluded() {
        OwnerWithSoldBook data = createOwnerAndSellOneBook("settled", "리팩터링", 35000);

        // 정산 전 - 미정산 1건
        assertThat(settlementService.findUnSettled(data.owner().getId())).hasSize(1);

        // 정산 실행
        settlementService.settle(settleRequest(data.owner().getId(), List.of(data.soldBookId())));

        // 정산 후 - 미정산 0건
        List<BookSoldRecordVO> result = settlementService.findUnSettled(data.owner().getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일부만 정산하면 나머지만 미정산으로 조회됨")
    void findUnSettled_partialSettle_remainingShown() {
        OwnerWithSoldBooks data = createOwnerAndSellMultipleBooks("partial",
                List.of("책A", "책B", "책C"),
                List.of(10000, 20000, 30000));
        List<Long> allIds = data.soldBookIds();

        // 첫 번째만 정산
        settlementService.settle(settleRequest(data.owner().getId(), List.of(allIds.get(0))));

        List<BookSoldRecordVO> result = settlementService.findUnSettled(data.owner().getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BookSoldRecordVO::getId)
                .containsExactlyInAnyOrder(allIds.get(1), allIds.get(2));
    }

    @Test
    @DisplayName("존재하지 않는 책소유주 ID로 조회하면 빈 리스트")
    void findUnSettled_nonExistentOwner_returnsEmpty() {
        List<BookSoldRecordVO> result = settlementService.findUnSettled(999999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("BookOwnerService를 통해서도 동일하게 조회됨")
    void findMyUnSettled_viaBokOwnerService() {
        OwnerWithSoldBooks data = createOwnerAndSellMultipleBooks("svc",
                List.of("책X", "책Y"),
                List.of(15000, 25000));

        List<BookSoldRecordVO> result = bookOwnerService.findMyUnSettled(data.owner().getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BookSoldRecordVO::getId)
                .containsExactlyInAnyOrderElementsOf(data.soldBookIds());
    }
}

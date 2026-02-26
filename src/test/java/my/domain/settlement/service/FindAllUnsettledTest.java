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
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.service.BookSoldRecordService;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.customer.service.auth.CustomerAuthService;
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
class FindAllUnsettledTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private BookSoldRecordService bookSoldRecordService;

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
    private int baselineUnsettledCount;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        // 기존 미정산 건수 기록 (시드 데이터 등 기존 데이터 보정용)
        baselineUnsettledCount = settlementService.findAllUnsettled().size();

        // 정산 비율 설정
        SettlementRatioVO ratioVO = new SettlementRatioVO();
        ratioVO.setOwnerRatio(0.7);
        ratioVO.setStoreRatio(0.3);
        settlementRatioService.create(ratioVO);

        // 고객 생성
        String custCode = uniqueCode();
        UserVO customer = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("테스트고객")
                .email("unsettled-cust-" + custCode + "@test.com")
                .phone("010-" + custCode.substring(0, 4) + "-" + custCode.substring(4))
                .password("password123")
                .residentNumber(custCode + "-2345678")
                .build());
        customerId = customer.getId();
    }

    private BookOwnerVO createBookOwnerWithBook(String emailPrefix, String bookName, int price) {
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

        // BookOwner 생성 + 책장 점유 (이름/전화번호를 고유하게)
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
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3), 50000);

        // 책 등록
        BookRegisterDto bookDto = new BookRegisterDto();
        bookDto.setUserName(ownerName);
        bookDto.setUserPhone(ownerPhone);
        bookDto.setBookName(bookName);
        bookDto.setPublisherHouse("출판사");
        bookDto.setPrice(price);
        bookDto.setBookTypeCode("04");
        bookCaseService.registerBooks(bookCaseId, List.of(bookDto));

        return owner;
    }

    private Long getBookIdByOwner(BookOwnerVO owner) {
        List<BookVO> books = bookMapper.selectBooksByBookOwnerId(owner.getId());
        return books.get(0).getId();
    }

    private BuyBookRequestDto createBuyDto(Long bookId) {
        BuyBookRequestDto dto = new BuyBookRequestDto();
        dto.setBookId(bookId);
        dto.setCustomerId(customerId);
        dto.setBuyTypeCommonCode("01");
        return dto;
    }

    @Test
    @DisplayName("판매 기록이 없으면 미정산 내역은 기존 건수와 동일")
    void findAllUnsettled_noSaleRecords_returnsEmpty() {
        List<BookSoldRecordVO> result = settlementService.findAllUnsettled();

        assertThat(result).hasSize(baselineUnsettledCount);
    }

    @Test
    @DisplayName("책 1건 판매 후 미정산 내역 1건 증가")
    void findAllUnsettled_oneSale_returnsOne() {
        BookOwnerVO owner = createBookOwnerWithBook("single", "이펙티브 자바", 36000);
        Long bookId = getBookIdByOwner(owner);

        bookSoldRecordService.sellBooks(List.of(createBuyDto(bookId)));

        List<BookSoldRecordVO> result = settlementService.findAllUnsettled();

        assertThat(result).hasSize(baselineUnsettledCount + 1);
        assertThat(result).extracting(BookSoldRecordVO::getId).contains(bookId);
        BookSoldRecordVO record = result.stream().filter(r -> r.getId().equals(bookId)).findFirst().orElseThrow();
        assertThat(record.getBookOwnerSettlementId()).isNull();
    }

    @Test
    @DisplayName("여러 책주인의 판매 기록이 모두 미정산 내역에 포함")
    void findAllUnsettled_multipleSales_returnsAll() {
        BookOwnerVO owner1 = createBookOwnerWithBook("multi1", "자바의 정석", 30000);
        BookOwnerVO owner2 = createBookOwnerWithBook("multi2", "스프링 인 액션", 40000);
        Long bookId1 = getBookIdByOwner(owner1);
        Long bookId2 = getBookIdByOwner(owner2);

        bookSoldRecordService.sellBooks(List.of(createBuyDto(bookId1)));
        bookSoldRecordService.sellBooks(List.of(createBuyDto(bookId2)));

        List<BookSoldRecordVO> result = settlementService.findAllUnsettled();

        assertThat(result).hasSize(baselineUnsettledCount + 2);
        assertThat(result).extracting(BookSoldRecordVO::getId)
                .contains(bookId1, bookId2);
    }

    @Test
    @DisplayName("미정산 레코드의 soldPrice가 정확히 기록되어 있는지 확인")
    void findAllUnsettled_soldPrice_correct() {
        BookOwnerVO owner = createBookOwnerWithBook("price", "클린 코드", 25000);
        Long bookId = getBookIdByOwner(owner);

        bookSoldRecordService.sellBooks(List.of(createBuyDto(bookId)));

        List<BookSoldRecordVO> result = settlementService.findAllUnsettled();

        assertThat(result).hasSize(baselineUnsettledCount + 1);
        BookSoldRecordVO record = result.stream().filter(r -> r.getId().equals(bookId)).findFirst().orElseThrow();
        assertThat(record.getSoldPrice()).isEqualTo(25000);
    }

    @Test
    @DisplayName("미정산 레코드에 ratioId가 기록되어 있는지 확인")
    void findAllUnsettled_ratioId_notNull() {
        BookOwnerVO owner = createBookOwnerWithBook("ratio", "리팩터링", 35000);
        Long bookId = getBookIdByOwner(owner);

        bookSoldRecordService.sellBooks(List.of(createBuyDto(bookId)));

        List<BookSoldRecordVO> result = settlementService.findAllUnsettled();

        assertThat(result).hasSize(baselineUnsettledCount + 1);
        BookSoldRecordVO record = result.stream().filter(r -> r.getId().equals(bookId)).findFirst().orElseThrow();
        assertThat(record.getRatioId()).isNotNull();
    }
}

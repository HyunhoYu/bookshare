package my.domain.booksoldrecord.service;

import my.common.exception.ApplicationException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SellBooksTest {

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

    private Long bookId;
    private Long customerId;
    private Long ratioId;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        // 정산 비율 설정
        SettlementRatioVO ratioVO = new SettlementRatioVO();
        ratioVO.setOwnerRatio(0.7);
        ratioVO.setStoreRatio(0.3);
        ratioId = settlementRatioService.create(ratioVO);

        // 책장 타입 생성
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(uniqueCode());
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        // 책장 생성
        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        // BookOwner 생성 + 책장 점유
        String ownerCode = uniqueCode();
        String ownerName = "홍길동" + ownerCode;
        String ownerPhone = "010-" + ownerCode.substring(0, 4) + "-" + ownerCode.substring(4);
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName)
                .email("sell-" + ownerCode + "@test.com")
                .phone(ownerPhone)
                .password("password123")
                .residentNumber(ownerCode + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));

        // 책 등록
        BookRegisterDto bookDto = new BookRegisterDto();
        bookDto.setUserName(ownerName);
        bookDto.setUserPhone(ownerPhone);
        bookDto.setBookName("이펙티브 자바");
        bookDto.setPublisherHouse("인사이트");
        bookDto.setPrice(36000);
        bookDto.setBookTypeCode("04");

        List<BookVO> books = bookCaseService.registerBooks(bookCaseId, List.of(bookDto));
        bookId = books.get(0).getId();

        // 고객 생성
        String custCode = uniqueCode();
        UserVO customer = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("김고객")
                .email("cust-" + custCode + "@test.com")
                .phone("010-" + custCode.substring(0, 4) + "-" + custCode.substring(4))
                .password("password123")
                .residentNumber(custCode + "-2345678")
                .build());
        customerId = customer.getId();
    }

    private BuyBookRequestDto createBuyDto(Long bookId, Long customerId, String buyType) {
        BuyBookRequestDto dto = new BuyBookRequestDto();
        dto.setBookId(bookId);
        dto.setCustomerId(customerId);
        dto.setBuyTypeCommonCode(buyType);
        return dto;
    }

    @Test
    @DisplayName("책 판매 성공 - 단건")
    void sellBooks_single_success() {
        List<BuyBookRequestDto> dtos = List.of(
                createBuyDto(bookId, customerId, "01")
        );

        List<BookSoldRecordVO> result = bookSoldRecordService.sellBooks(dtos);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(bookId);
        assertThat(result.get(0).getSoldPrice()).isEqualTo(36000);
        assertThat(result.get(0).getCustomerId()).isEqualTo(customerId);
        assertThat(result.get(0).getCommonCodeId()).isEqualTo("01");
        assertThat(result.get(0).getRatioId()).isNotNull();
        assertThat(result.get(0).getBookOwnerSettlementId()).isNull();
    }

    @Test
    @DisplayName("책 판매 후 상태가 SOLD로 변경")
    void sellBooks_bookState_changed_to_sold() {
        bookSoldRecordService.sellBooks(List.of(
                createBuyDto(bookId, customerId, "01")
        ));

        BookVO soldBook = bookMapper.selectById(bookId);
        assertThat(soldBook.getState()).isEqualTo("SOLD");
    }

    @Test
    @DisplayName("책 판매 시 현재 정산비율 ID가 기록됨")
    void sellBooks_ratioId_recorded() {
        List<BookSoldRecordVO> result = bookSoldRecordService.sellBooks(List.of(
                createBuyDto(bookId, customerId, "01")
        ));

        assertThat(result.get(0).getRatioId()).isEqualTo(ratioId);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 책")
    void sellBooks_bookNotFound() {
        List<BuyBookRequestDto> dtos = List.of(
                createBuyDto(999999L, customerId, "01")
        );

        assertThatThrownBy(() -> bookSoldRecordService.sellBooks(dtos))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("실패 - 이미 판매된 책 재판매 시도")
    void sellBooks_alreadySold() {
        bookSoldRecordService.sellBooks(List.of(
                createBuyDto(bookId, customerId, "01")
        ));

        List<BuyBookRequestDto> dtos = List.of(
                createBuyDto(bookId, customerId, "01")
        );

        assertThatThrownBy(() -> bookSoldRecordService.sellBooks(dtos))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("실패 - 빈 목록으로 판매 요청")
    void sellBooks_emptyList() {
        assertThatThrownBy(() -> bookSoldRecordService.sellBooks(List.of()))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("실패 - null로 판매 요청")
    void sellBooks_null() {
        assertThatThrownBy(() -> bookSoldRecordService.sellBooks(null))
                .isInstanceOf(ApplicationException.class);
    }
}

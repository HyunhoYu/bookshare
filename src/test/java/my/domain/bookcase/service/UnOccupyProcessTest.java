package my.domain.bookcase.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.service.BookSoldRecordService;
import my.domain.customer.service.auth.CustomerAuthService;
import my.domain.settlement_ratio.service.SettlementRatioService;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserJoinRequestDto;
import my.enums.BookState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class UnOccupyProcessTest {

    @Autowired private BookCaseService bookCaseService;
    @Autowired private BookCaseTypeService bookCaseTypeService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;
    @Autowired private BookSoldRecordService bookSoldRecordService;
    @Autowired private CustomerAuthService customerAuthService;
    @Autowired private SettlementRatioService settlementRatioService;
    @Autowired private BookMapper bookMapper;

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
                .email("unocc-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
    }

    private List<BookVO> registerBooks(long bookCaseId, BookOwnerVO owner, int count) {
        String ownerName = owner.getName();
        String ownerPhone = owner.getPhone();

        List<BookRegisterDto> dtos = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            BookRegisterDto dto = new BookRegisterDto();
            dto.setUserName(ownerName);
            dto.setUserPhone(ownerPhone);
            dto.setBookName("Book" + uniqueCode());
            dto.setPublisherHouse("Publisher");
            dto.setPrice(10000 + i * 5000);
            dto.setBookTypeCode("04");
            dtos.add(dto);
        }
        return bookCaseService.registerBooks(bookCaseId, dtos);
    }

    private Long createCustomer() {
        String code = uniqueCode();
        UserVO customer = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("cust" + code)
                .email("cust-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123")
                .residentNumber(code + "-2345678")
                .build());
        return customer.getId();
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCase {

        @Test
        @DisplayName("책장 1개 점유 해제 - NORMAL 상태 책들이 SHOULD_BE_RETRIEVED로 변경")
        void unOccupy_single_booksChangeState() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));
            List<BookVO> books = registerBooks(bookCaseId, owner, 2);

            List<Long> changedBookIds = bookCaseService.unOccupyProcess(List.of(bookCaseId));

            assertThat(changedBookIds).hasSize(2);
            for (Long bookId : changedBookIds) {
                BookVO book = bookMapper.selectById(bookId);
                assertThat(book.getState()).isEqualTo(BookState.SHOULD_BE_RETRIEVED.name());
            }
        }

        @Test
        @DisplayName("책장 여러 개 점유 해제 - 각 책장의 NORMAL 책들 전부 상태 변경")
        void unOccupy_multiple_allBooksChangeState() {
            long caseId1 = createBookCase();
            long caseId2 = createBookCase();
            BookOwnerVO owner1 = createBookOwner();
            BookOwnerVO owner2 = createBookOwner();

            bookCaseService.occupy(owner1.getId(), List.of(caseId1), LocalDate.now().plusMonths(3));
            bookCaseService.occupy(owner2.getId(), List.of(caseId2), LocalDate.now().plusMonths(3));
            registerBooks(caseId1, owner1, 1);
            registerBooks(caseId2, owner2, 2);

            List<Long> changedBookIds = bookCaseService.unOccupyProcess(List.of(caseId1, caseId2));

            assertThat(changedBookIds).hasSize(3);
            for (Long bookId : changedBookIds) {
                BookVO book = bookMapper.selectById(bookId);
                assertThat(book.getState()).isEqualTo(BookState.SHOULD_BE_RETRIEVED.name());
            }
        }

        @Test
        @DisplayName("점유 해제 후 책장이 다시 이용 가능 상태")
        void unOccupy_bookCaseBecomesUsable() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));

            bookCaseService.unOccupyProcess(List.of(bookCaseId));

            assertThat(bookCaseService.isOccupied(bookCaseId)).isFalse();
            List<BookCaseVO> usable = bookCaseService.findUsable();
            assertThat(usable).extracting(BookCaseVO::getId).contains(bookCaseId);
        }

        @Test
        @DisplayName("모든 책이 SOLD인 책장 점유 해제 - 빈 리스트 반환")
        void unOccupy_allBooksSold_returnsEmpty() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));
            List<BookVO> books = registerBooks(bookCaseId, owner, 1);

            Long customerId = createCustomer();
            BuyBookRequestDto buyDto = new BuyBookRequestDto();
            buyDto.setBookId(books.get(0).getId());
            buyDto.setCustomerId(customerId);
            buyDto.setBuyTypeCommonCode("01");
            bookSoldRecordService.sellBooks(List.of(buyDto));

            List<Long> changedBookIds = bookCaseService.unOccupyProcess(List.of(bookCaseId));

            assertThat(changedBookIds).isEmpty();
        }

        @Test
        @DisplayName("책이 없는 책장 점유 해제 - 빈 리스트 반환")
        void unOccupy_noBooks_returnsEmpty() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));

            List<Long> changedBookIds = bookCaseService.unOccupyProcess(List.of(bookCaseId));

            assertThat(changedBookIds).isEmpty();
        }

        @Test
        @DisplayName("SOLD와 NORMAL 책이 섞여있으면 NORMAL 책만 상태 변경")
        void unOccupy_mixedStates_onlyNormalChanged() {
            long bookCaseId = createBookCase();
            BookOwnerVO owner = createBookOwner();
            bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3));
            List<BookVO> books = registerBooks(bookCaseId, owner, 2);

            Long customerId = createCustomer();
            BuyBookRequestDto buyDto = new BuyBookRequestDto();
            buyDto.setBookId(books.get(0).getId());
            buyDto.setCustomerId(customerId);
            buyDto.setBuyTypeCommonCode("01");
            bookSoldRecordService.sellBooks(List.of(buyDto));

            List<Long> changedBookIds = bookCaseService.unOccupyProcess(List.of(bookCaseId));

            assertThat(changedBookIds).hasSize(1);
            assertThat(changedBookIds).contains(books.get(1).getId());
            assertThat(changedBookIds).doesNotContain(books.get(0).getId());

            BookVO soldBook = bookMapper.selectById(books.get(0).getId());
            assertThat(soldBook.getState()).isEqualTo(BookState.SOLD.name());
        }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailCase {

        @Test
        @DisplayName("실패 - bookCaseIds가 null")
        void unOccupy_fail_nullIds() {
            assertThatThrownBy(() -> bookCaseService.unOccupyProcess(null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.EMPTY_UNOCCUPY_REQUEST.getMessage());
        }

        @Test
        @DisplayName("실패 - bookCaseIds가 빈 리스트")
        void unOccupy_fail_emptyIds() {
            assertThatThrownBy(() -> bookCaseService.unOccupyProcess(List.of()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.EMPTY_UNOCCUPY_REQUEST.getMessage());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 책장 ID")
        void unOccupy_fail_bookCaseNotFound() {
            assertThatThrownBy(() -> bookCaseService.unOccupyProcess(List.of(999999L)))
                    .isInstanceOf(ApplicationException.class);
        }

        @Test
        @DisplayName("실패 - 점유 중이 아닌 책장")
        void unOccupy_fail_notOccupied() {
            long bookCaseId = createBookCase();

            assertThatThrownBy(() -> bookCaseService.unOccupyProcess(List.of(bookCaseId)))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.BOOK_CASE_NOT_OCCUPIED.getMessage());
        }
    }
}

package my.domain.book;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcase.service.BookCaseService;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
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
class RetrieveBooksTest {

    @Autowired private BookService bookService;
    @Autowired private BookMapper bookMapper;
    @Autowired private BookCaseService bookCaseService;
    @Autowired private BookCaseTypeService bookCaseTypeService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;

    private long bookCaseId;
    private BookOwnerVO owner;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        String code = uniqueCode();

        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        bookCaseId = bookCaseService.create(caseDto);

        owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("owner" + code)
                .email("retrieve-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3), 50000);
    }

    private List<BookVO> registerBooks(int count) {
        List<BookRegisterDto> dtos = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            BookRegisterDto dto = new BookRegisterDto();
            dto.setUserName(owner.getName());
            dto.setUserPhone(owner.getPhone());
            dto.setBookName("Book" + uniqueCode());
            dto.setPublisherHouse("Publisher");
            dto.setPrice(10000 + i * 5000);
            dto.setBookTypeCode("04");
            dtos.add(dto);
        }
        return bookCaseService.registerBooks(bookCaseId, dtos);
    }

    private List<Long> registerAndMakeShouldBeRetrieved(int count) {
        List<BookVO> books = registerBooks(count);
        List<Long> bookIds = books.stream().map(BookVO::getId).toList();
        bookMapper.updateStateNormalToRetrieve(bookIds);
        return bookIds;
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCase {

        @Test
        @DisplayName("회수 성공 - 단건, DELETED_AT이 설정됨")
        void retrieve_single_deletedAtSet() {
            List<Long> bookIds = registerAndMakeShouldBeRetrieved(1);

            List<Long> result = bookService.retrieveBooks(bookIds);

            assertThat(result).hasSize(1);
            BookVO book = bookMapper.selectByIdIncludeDeleted(bookIds.get(0));
            assertThat(book.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("회수 성공 - 다건, 모든 책의 DELETED_AT이 설정됨")
        void retrieve_multiple_allDeletedAtSet() {
            List<Long> bookIds = registerAndMakeShouldBeRetrieved(3);

            List<Long> result = bookService.retrieveBooks(bookIds);

            assertThat(result).hasSize(3);
            for (Long bookId : bookIds) {
                BookVO book = bookMapper.selectByIdIncludeDeleted(bookId);
                assertThat(book.getDeletedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("회수 후 재고 목록(findAll)에서 제외됨")
        void retrieve_excludedFromStock() {
            List<BookVO> normalBooks = registerBooks(1);
            Long normalBookId = normalBooks.get(0).getId();

            assertThat(bookService.findAll()).extracting(BookVO::getId).contains(normalBookId);

            bookMapper.updateStateNormalToRetrieve(List.of(normalBookId));
            bookService.retrieveBooks(List.of(normalBookId));

            assertThat(bookService.findAll()).extracting(BookVO::getId).doesNotContain(normalBookId);
        }

        @Test
        @DisplayName("회수 후에도 STATE는 SHOULD_BE_RETRIEVED 유지")
        void retrieve_stateUnchanged() {
            List<Long> bookIds = registerAndMakeShouldBeRetrieved(1);

            bookService.retrieveBooks(bookIds);

            BookVO book = bookMapper.selectByIdIncludeDeleted(bookIds.get(0));
            assertThat(book.getState()).isEqualTo(BookState.SHOULD_BE_RETRIEVED.name());
            assertThat(book.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("unOccupyProcess → retrieveBooks 전체 플로우")
        void fullFlow_unOccupyThenRetrieve() {
            List<BookVO> books = registerBooks(2);
            List<Long> allBookIds = books.stream().map(BookVO::getId).toList();

            List<Long> changedBookIds = bookCaseService.unOccupyProcess(List.of(bookCaseId));

            assertThat(changedBookIds).containsExactlyInAnyOrderElementsOf(allBookIds);

            List<Long> result = bookService.retrieveBooks(changedBookIds);

            assertThat(result).hasSize(2);
            for (Long bookId : result) {
                BookVO book = bookMapper.selectByIdIncludeDeleted(bookId);
                assertThat(book.getDeletedAt()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailCase {

        @Test
        @DisplayName("실패 - bookIds가 null")
        void retrieve_fail_nullIds() {
            assertThatThrownBy(() -> bookService.retrieveBooks(null))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.EMPTY_RETRIEVE_REQUEST.getMessage());
        }

        @Test
        @DisplayName("실패 - bookIds가 빈 리스트")
        void retrieve_fail_emptyIds() {
            assertThatThrownBy(() -> bookService.retrieveBooks(List.of()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.EMPTY_RETRIEVE_REQUEST.getMessage());
        }

        @Test
        @DisplayName("실패 - NORMAL 상태인 책 회수 시도")
        void retrieve_fail_normalState() {
            List<BookVO> books = registerBooks(1);
            List<Long> bookIds = books.stream().map(BookVO::getId).toList();

            assertThatThrownBy(() -> bookService.retrieveBooks(bookIds))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.BOOK_NOT_RETRIEVABLE.getMessage());
        }

        @Test
        @DisplayName("실패 - SHOULD_BE_RETRIEVED와 NORMAL 상태 혼합")
        void retrieve_fail_mixedStates() {
            List<BookVO> books = registerBooks(2);
            Long bookId1 = books.get(0).getId();
            Long bookId2 = books.get(1).getId();

            bookMapper.updateStateNormalToRetrieve(List.of(bookId1));

            assertThatThrownBy(() -> bookService.retrieveBooks(List.of(bookId1, bookId2)))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.BOOK_NOT_RETRIEVABLE.getMessage());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 책 ID")
        void retrieve_fail_nonExistentBookId() {
            assertThatThrownBy(() -> bookService.retrieveBooks(List.of(999999L)))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.BOOK_NOT_RETRIEVABLE.getMessage());
        }

        @Test
        @DisplayName("실패 - 이미 soft delete된 책 재회수 시도")
        void retrieve_fail_alreadyDeleted() {
            List<Long> bookIds = registerAndMakeShouldBeRetrieved(1);

            bookService.retrieveBooks(bookIds);

            assertThatThrownBy(() -> bookService.retrieveBooks(bookIds))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(ErrorCode.BOOK_NOT_RETRIEVABLE.getMessage());
        }
    }
}

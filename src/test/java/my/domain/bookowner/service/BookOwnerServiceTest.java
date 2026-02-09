package my.domain.bookowner.service;

import my.domain.book.BookService;
import my.domain.book.BookVO;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.settlement.service.SettlementService;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.settlement.vo.SettlementVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookOwnerServiceTest {

    @Mock
    private BookOwnerMapper bookOwnerMapper;

    @Mock
    private BookService bookService;

    @Mock
    private SettlementService settlementService;

    @InjectMocks
    private BookOwnerServiceImpl bookOwnerService;

    private BookOwnerVO testBookOwner;
    private List<BookVO> testBooks;
    private List<SettlementVO> testSettlements;

    @BeforeEach
    void setUp() {
        // 테스트용 BookOwner 생성
        testBookOwner = new BookOwnerVO();
        testBookOwner.setId(1L);
        testBookOwner.setName("홍길동");
        testBookOwner.setEmail("hong@test.com");

        // 테스트용 Book 목록 생성
        BookVO book1 = new BookVO();
        book1.setId(1L);
        book1.setBookName("어린왕자");
        book1.setPrice(10000);
        book1.setBookOwnerId(1L);

        BookVO book2 = new BookVO();
        book2.setId(2L);
        book2.setBookName("데미안");
        book2.setPrice(12000);
        book2.setBookOwnerId(1L);

        testBooks = Arrays.asList(book1, book2);

        // 테스트용 Settlement 목록 생성
        SettlementVO settlement1 = new SettlementVO();
        settlement1.setId(1L);
        settlement1.setBookOwnerId(1L);
        settlement1.setSettledAt(new Timestamp(System.currentTimeMillis()));

        testSettlements = Arrays.asList(settlement1);
    }

    @Nested
    @DisplayName("findAll() 테스트")
    class FindAllTest {

        @Test
        @DisplayName("전체 책소유주 목록을 반환한다")
        void findAll_ReturnsAllBookOwners() {
            // given
            List<BookOwnerVO> bookOwners = Arrays.asList(testBookOwner);
            given(bookOwnerMapper.selectAll()).willReturn(bookOwners);

            // when
            List<BookOwnerVO> result = bookOwnerService.findAll();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("홍길동");
            verify(bookOwnerMapper).selectAll();
        }

        @Test
        @DisplayName("책소유주가 없으면 빈 리스트를 반환한다")
        void findAll_WhenNoBookOwners_ReturnsEmptyList() {
            // given
            given(bookOwnerMapper.selectAll()).willReturn(Collections.emptyList());

            // when
            List<BookOwnerVO> result = bookOwnerService.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findOne() 테스트")
    class FindOneTest {

        @Test
        @DisplayName("ID로 책소유주를 조회한다")
        void findOne_WithValidId_ReturnsBookOwner() {
            // given
            given(bookOwnerMapper.selectOne(1L)).willReturn(testBookOwner);

            // when
            BookOwnerVO result = bookOwnerService.findOne(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("홍길동");
            verify(bookOwnerMapper).selectOne(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 null을 반환한다")
        void findOne_WithInvalidId_ReturnsNull() {
            // given
            given(bookOwnerMapper.selectOne(999L)).willReturn(null);

            // when
            BookOwnerVO result = bookOwnerService.findOne(999L);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("findMyBooks() 테스트")
    class FindMyBooksTest {

        @Test
        @DisplayName("책소유주의 책 목록을 반환한다")
        void findMyBooks_ReturnsBookOwnerBooks() {
            // given
            given(bookService.findBooksByBookOwnerId(1L)).willReturn(testBooks);

            // when
            List<BookVO> result = bookOwnerService.findMyBooks(1L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getBookName()).isEqualTo("어린왕자");
            verify(bookService).findBooksByBookOwnerId(1L);
        }

        @Test
        @DisplayName("등록한 책이 없으면 빈 리스트를 반환한다")
        void findMyBooks_WhenNoBooks_ReturnsEmptyList() {
            // given
            given(bookService.findBooksByBookOwnerId(1L)).willReturn(Collections.emptyList());

            // when
            List<BookVO> result = bookOwnerService.findMyBooks(1L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findMySoldBooks() 테스트")
    class FindMySoldBooksTest {

        @Test
        @DisplayName("책소유주의 판매된 책 목록을 반환한다")
        void findMySoldBooks_ReturnsSoldBooks() {
            // given
            given(bookService.findSoldBookOfBookOwner(1L)).willReturn(testBooks);

            // when
            List<BookVO> result = bookOwnerService.findMySoldBooks(1L);

            // then
            assertThat(result).hasSize(2);
            verify(bookService).findSoldBookOfBookOwner(1L);
        }
    }

    @Nested
    @DisplayName("findAllMySettlements() 테스트")
    class FindAllMySettlementsTest {

        @Test
        @DisplayName("책소유주의 전체 정산 내역을 반환한다")
        void findAllMySettlements_ReturnsAllSettlements() {
            // given
            given(settlementService.findAll(1L)).willReturn(testSettlements);

            // when
            List<SettlementVO> result = bookOwnerService.findAllMySettlements(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBookOwnerId()).isEqualTo(1L);
            verify(settlementService).findAll(1L);
        }
    }

    @Nested
    @DisplayName("findMySettled() 테스트")
    class FindMySettledTest {

        @Test
        @DisplayName("책소유주의 정산 완료 내역을 반환한다")
        void findMySettled_ReturnsSettledRecords() {
            // given
            given(settlementService.findSettled(1L)).willReturn(testSettlements);

            // when
            List<SettlementVO> result = bookOwnerService.findMySettled(1L);

            // then
            assertThat(result).hasSize(1);
            verify(settlementService).findSettled(1L);
        }
    }

    @Nested
    @DisplayName("findMyUnSettled() 테스트")
    class FindMyUnSettledTest {

        @Test
        @DisplayName("책소유주의 미정산 내역을 반환한다")
        void findMyUnSettled_ReturnsUnSettledRecords() {
            // given
            given(settlementService.findUnSettled(1L)).willReturn(Collections.emptyList());

            // when
            List<BookSoldRecordVO> result = bookOwnerService.findMyUnSettled(1L);

            // then
            assertThat(result).isEmpty();
            verify(settlementService).findUnSettled(1L);
        }
    }
}

package my.domain.bookowner_post.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book.BookVO;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcase.service.BookCaseService;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.bookowner_post.dto.BookOwnerPostCreateDto;
import my.domain.bookowner_post.dto.BookOwnerPostUpdateDto;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookOwnerPostServiceTest {

    @Autowired private BookOwnerPostService postService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;
    @Autowired private BookCaseService bookCaseService;
    @Autowired private BookCaseTypeService bookCaseTypeService;
    @Autowired private SettlementRatioService settlementRatioService;

    private Long ownerId;
    private Long ownerId2;
    private Long bookId;
    private String ownerName;
    private String ownerPhone;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        SettlementRatioVO ratioVO = new SettlementRatioVO();
        ratioVO.setOwnerRatio(0.7);
        ratioVO.setStoreRatio(0.3);
        settlementRatioService.create(ratioVO);

        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(uniqueCode());
        typeDto.setMonthlyPrice(50000);
        long typeId = bookCaseTypeService.create(typeDto);

        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        long bookCaseId = bookCaseService.create(caseDto);

        // owner1
        String code1 = uniqueCode();
        ownerName = "테스트" + code1;
        ownerPhone = "010-" + code1.substring(0, 4) + "-" + code1.substring(4);
        BookOwnerVO owner1 = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name(ownerName).email("post-" + code1 + "@test.com").phone(ownerPhone)
                .password("password123").residentNumber(code1 + "-1234567")
                .bankName("국민은행").accountNumber("123-456-789").build());
        ownerId = owner1.getId();

        // owner2
        String code2 = uniqueCode();
        BookOwnerVO owner2 = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("테스트" + code2).email("post-" + code2 + "@test.com")
                .phone("010-" + code2.substring(0, 4) + "-" + code2.substring(4))
                .password("password123").residentNumber(code2 + "-1234567")
                .bankName("국민은행").accountNumber("123-456-789").build());
        ownerId2 = owner2.getId();

        bookCaseService.occupy(ownerId, List.of(bookCaseId), LocalDate.now().plusMonths(3), 50000);

        BookRegisterDto bookDto = new BookRegisterDto();
        bookDto.setUserName(ownerName);
        bookDto.setUserPhone(ownerPhone);
        bookDto.setBookName("테스트 책");
        bookDto.setPublisherHouse("출판사");
        bookDto.setPrice(15000);
        bookDto.setBookTypeCode("04");

        List<BookVO> books = bookCaseService.registerBooks(bookCaseId, List.of(bookDto));
        bookId = books.get(0).getId();
    }

    @Nested
    @DisplayName("게시글 생성")
    class CreateTest {

        @Test
        @DisplayName("성공 - 책 연결 없이 게시글 생성")
        void create_withoutBook() {
            BookOwnerPostCreateDto dto = new BookOwnerPostCreateDto();
            dto.setTitle("첫 번째 게시글");
            dto.setContent("안녕하세요, 저의 서재를 소개합니다.");

            BookOwnerPostVO result = postService.create(ownerId, dto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getTitle()).isEqualTo("첫 번째 게시글");
            assertThat(result.getContent()).isEqualTo("안녕하세요, 저의 서재를 소개합니다.");
            assertThat(result.getBookId()).isNull();
            assertThat(result.getBookOwnerName()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 책 연결하여 게시글 생성")
        void create_withBook() {
            BookOwnerPostCreateDto dto = new BookOwnerPostCreateDto();
            dto.setTitle("이 책을 추천합니다");
            dto.setContent("정말 좋은 책입니다.");
            dto.setBookId(bookId);

            BookOwnerPostVO result = postService.create(ownerId, dto);

            assertThat(result.getBookId()).isEqualTo(bookId);
            assertThat(result.getBookName()).isEqualTo("테스트 책");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 책 연결")
        void create_bookNotFound() {
            BookOwnerPostCreateDto dto = new BookOwnerPostCreateDto();
            dto.setTitle("게시글");
            dto.setBookId(999999L);

            assertThatThrownBy(() -> postService.create(ownerId, dto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 다른 소유주의 책 연결")
        void create_otherOwnersBook() {
            BookOwnerPostCreateDto dto = new BookOwnerPostCreateDto();
            dto.setTitle("게시글");
            dto.setBookId(bookId);

            assertThatThrownBy(() -> postService.create(ownerId2, dto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.POST_BOOK_OWNER_MISMATCH);
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdateTest {

        @Test
        @DisplayName("성공 - 게시글 수정")
        void update_success() {
            BookOwnerPostCreateDto createDto = new BookOwnerPostCreateDto();
            createDto.setTitle("원래 제목");
            createDto.setContent("원래 내용");
            BookOwnerPostVO created = postService.create(ownerId, createDto);

            BookOwnerPostUpdateDto updateDto = new BookOwnerPostUpdateDto();
            updateDto.setTitle("수정된 제목");
            updateDto.setContent("수정된 내용");

            BookOwnerPostVO result = postService.update(created.getId(), ownerId, updateDto);

            assertThat(result.getTitle()).isEqualTo("수정된 제목");
            assertThat(result.getContent()).isEqualTo("수정된 내용");
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 다른 소유주가 수정 시도")
        void update_forbidden() {
            BookOwnerPostCreateDto createDto = new BookOwnerPostCreateDto();
            createDto.setTitle("제목");
            BookOwnerPostVO created = postService.create(ownerId, createDto);

            BookOwnerPostUpdateDto updateDto = new BookOwnerPostUpdateDto();
            updateDto.setTitle("수정");

            assertThatThrownBy(() -> postService.update(created.getId(), ownerId2, updateDto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글 수정")
        void update_notFound() {
            BookOwnerPostUpdateDto updateDto = new BookOwnerPostUpdateDto();
            updateDto.setTitle("수정");

            assertThatThrownBy(() -> postService.update(999999L, ownerId, updateDto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeleteTest {

        @Test
        @DisplayName("성공 - soft delete 후 조회 불가")
        void delete_success() {
            BookOwnerPostCreateDto createDto = new BookOwnerPostCreateDto();
            createDto.setTitle("삭제할 게시글");
            BookOwnerPostVO created = postService.create(ownerId, createDto);

            postService.delete(created.getId(), ownerId);

            assertThat(postService.findById(created.getId())).isNull();
        }

        @Test
        @DisplayName("실패 - 다른 소유주가 삭제 시도")
        void delete_forbidden() {
            BookOwnerPostCreateDto createDto = new BookOwnerPostCreateDto();
            createDto.setTitle("게시글");
            BookOwnerPostVO created = postService.create(ownerId, createDto);

            assertThatThrownBy(() -> postService.delete(created.getId(), ownerId2))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("게시글 조회")
    class FindTest {

        @Test
        @DisplayName("전체 게시글 조회 - 최신순")
        void findAll() {
            BookOwnerPostCreateDto dto1 = new BookOwnerPostCreateDto();
            dto1.setTitle("첫번째");
            postService.create(ownerId, dto1);

            BookOwnerPostCreateDto dto2 = new BookOwnerPostCreateDto();
            dto2.setTitle("두번째");
            postService.create(ownerId, dto2);

            List<BookOwnerPostVO> result = postService.findAll();

            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
            assertThat(result.get(0).getCreatedAt().getTime())
                    .isGreaterThanOrEqualTo(result.get(1).getCreatedAt().getTime());
        }

        @Test
        @DisplayName("특정 BookOwner 게시글만 조회")
        void findByBookOwnerId() {
            BookOwnerPostCreateDto dto = new BookOwnerPostCreateDto();
            dto.setTitle("owner1 게시글");
            postService.create(ownerId, dto);

            List<BookOwnerPostVO> result = postService.findByBookOwnerId(ownerId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBookOwnerId()).isEqualTo(ownerId);
        }

        @Test
        @DisplayName("삭제된 게시글은 조회되지 않음")
        void findAll_excludesDeleted() {
            BookOwnerPostCreateDto dto = new BookOwnerPostCreateDto();
            dto.setTitle("삭제될 게시글");
            BookOwnerPostVO created = postService.create(ownerId, dto);

            postService.delete(created.getId(), ownerId);

            List<BookOwnerPostVO> result = postService.findByBookOwnerId(ownerId);
            assertThat(result).isEmpty();
        }
    }
}

package my.domain.bookcase.service;

import my.common.exception.ApplicationException;
import my.common.exception.BookCaseNotFoundException;
import my.common.exception.BookOwnerMismatchException;
import my.common.exception.ForbiddenException;
import my.domain.book.BookVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcasetype.BookCaseTypeVO;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RegisterBooksTest {

    @Autowired
    private BookCaseService bookCaseService;

    @Autowired
    private BookCaseTypeService bookCaseTypeService;

    @Autowired
    private BookOwnerAuthService bookOwnerAuthService;

    private long bookCaseId;
    private long typeId;
    private BookOwnerVO owner;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        // 책장 타입 생성
        BookCaseTypeVO typeVO = new BookCaseTypeVO();
        typeVO.setCode(uniqueCode());
        typeVO.setMonthlyPrice(50000);
        typeId = bookCaseTypeService.addBookCaseType(typeVO);

        // 책장 생성
        BookCaseVO caseVO = new BookCaseVO();
        caseVO.setLocationCode("TEST-" + uniqueCode());
        caseVO.setBookCaseTypeId(typeId);
        bookCaseId = bookCaseService.addBookCase(caseVO);

        // BookOwner 생성
        owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("홍길동")
                .email("reg-" + uniqueCode() + "@test.com")
                .phone("010-1111-2222")
                .password("password123")
                .residentNumber("990101-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());

        // 책장 점유
        bookCaseService.occupy(owner.getId(), bookCaseId);
    }

    private BookRegisterDto createDto(String bookName, String publisherHouse, int price, String bookType) {
        BookRegisterDto dto = new BookRegisterDto();
        dto.setUserName("홍길동");
        dto.setUserPhone("010-1111-2222");
        dto.setBookName(bookName);
        dto.setPublisherHouse(publisherHouse);
        dto.setPrice(price);
        dto.setBookType(bookType);
        return dto;
    }

    @Test
    @DisplayName("책 등록 성공 - 단건")
    void registerBooks_single_success() {
        List<BookRegisterDto> dtos = List.of(
                createDto("이펙티브 자바", "인사이트", 36000, "과학")
        );

        List<BookVO> result = bookCaseService.registerBooks(bookCaseId, dtos);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookName()).isEqualTo("이펙티브 자바");
        assertThat(result.get(0).getPrice()).isEqualTo(36000);
        assertThat(result.get(0).getState()).isEqualTo("NORMAL");
        assertThat(result.get(0).getBookOwnerId()).isEqualTo(owner.getId());
        assertThat(result.get(0).getBookCaseId()).isEqualTo(bookCaseId);
    }

    @Test
    @DisplayName("책 등록 성공 - 다건")
    void registerBooks_multiple_success() {
        List<BookRegisterDto> dtos = List.of(
                createDto("이펙티브 자바", "인사이트", 36000, "과학"),
                createDto("삼국지", "민음사", 15000, "역사"),
                createDto("축구의 역사", "한빛미디어", 22000, "스포츠")
        );

        List<BookVO> result = bookCaseService.registerBooks(bookCaseId, dtos);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(BookVO::getBookName)
                .containsExactly("이펙티브 자바", "삼국지", "축구의 역사");
        assertThat(result).allSatisfy(book -> {
            assertThat(book.getBookOwnerId()).isEqualTo(owner.getId());
            assertThat(book.getBookCaseId()).isEqualTo(bookCaseId);
            assertThat(book.getState()).isEqualTo("NORMAL");
            assertThat(book.getEnteredAt()).isNotNull();
            assertThat(book.getCommonCodeId()).isNotNull();
        });
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 책장")
    void registerBooks_bookCaseNotFound() {
        List<BookRegisterDto> dtos = List.of(
                createDto("테스트책", "출판사", 10000, "소설")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(999999L, dtos))
                .isInstanceOf(BookCaseNotFoundException.class);
    }

    @Test
    @DisplayName("실패 - 소유주가 해당 책장을 점유하고 있지 않음")
    void registerBooks_notOccupiedByOwner() {
        // 새 책장 생성 (점유 안 함)
        BookCaseVO newCase = new BookCaseVO();
        newCase.setLocationCode("TEST-" + uniqueCode());
        newCase.setBookCaseTypeId(typeId);
        long unoccupiedCaseId = bookCaseService.addBookCase(newCase);

        List<BookRegisterDto> dtos = List.of(
                createDto("테스트책", "출판사", 10000, "소설")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(unoccupiedCaseId, dtos))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("실패 - 다른 소유주가 점유 중인 책장에 등록 시도")
    void registerBooks_occupiedByDifferentOwner() {
        // 다른 소유주 생성 + 새 책장 점유
        BookOwnerVO otherOwner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("김철수")
                .email("other-" + uniqueCode() + "@test.com")
                .phone("010-9999-8888")
                .password("password123")
                .residentNumber("880202-1234567")
                .bankName("신한은행")
                .accountNumber("999-888-777")
                .build());

        BookCaseVO newCase = new BookCaseVO();
        newCase.setLocationCode("TEST-" + uniqueCode());
        newCase.setBookCaseTypeId(typeId);
        long otherCaseId = bookCaseService.addBookCase(newCase);
        bookCaseService.occupy(otherOwner.getId(), otherCaseId);

        // 홍길동 이름으로 김철수의 책장에 등록 시도
        List<BookRegisterDto> dtos = List.of(
                createDto("테스트책", "출판사", 10000, "소설")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(otherCaseId, dtos))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("실패 - 소유주 정보가 리스트 내에서 불일치")
    void registerBooks_ownerMismatch() {
        BookRegisterDto dto1 = createDto("책1", "출판사", 10000, "소설");
        BookRegisterDto dto2 = createDto("책2", "출판사", 15000, "역사");
        dto2.setUserName("다른사람");

        List<BookRegisterDto> dtos = List.of(dto1, dto2);

        assertThatThrownBy(() -> bookCaseService.registerBooks(bookCaseId, dtos))
                .isInstanceOf(BookOwnerMismatchException.class);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 책 분류")
    void registerBooks_invalidBookType() {
        List<BookRegisterDto> dtos = List.of(
                createDto("테스트책", "출판사", 10000, "존재하지않는분류")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(bookCaseId, dtos))
                .isInstanceOf(ApplicationException.class);
    }
}

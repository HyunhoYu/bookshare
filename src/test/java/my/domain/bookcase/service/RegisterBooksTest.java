package my.domain.bookcase.service;

import my.common.exception.ApplicationException;
import my.domain.book.BookVO;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
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

import java.time.LocalDate;
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
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(uniqueCode());
        typeDto.setMonthlyPrice(50000);
        typeId = bookCaseTypeService.create(typeDto);

        // 책장 생성
        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        bookCaseId = bookCaseService.create(caseDto);

        // BookOwner 생성
        String ownerCode = uniqueCode();
        owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("홍길동")
                .email("reg-" + ownerCode + "@test.com")
                .phone("010-" + ownerCode.substring(0, 4) + "-" + ownerCode.substring(4))
                .password("password123")
                .residentNumber(ownerCode + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());

        // 책장 점유
        bookCaseService.occupy(owner.getId(), List.of(bookCaseId), LocalDate.now().plusMonths(3), 50000);
    }

    private BookRegisterDto createDto(String bookName, String publisherHouse, int price, String bookTypeCode) {
        BookRegisterDto dto = new BookRegisterDto();
        dto.setUserName(owner.getName());
        dto.setUserPhone(owner.getPhone());
        dto.setBookName(bookName);
        dto.setPublisherHouse(publisherHouse);
        dto.setPrice(price);
        dto.setBookTypeCode(bookTypeCode);
        return dto;
    }

    @Test
    @DisplayName("책 등록 성공 - 단건")
    void registerBooks_single_success() {
        List<BookRegisterDto> dtos = List.of(
                createDto("이펙티브 자바", "인사이트", 36000, "04")
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
                createDto("이펙티브 자바", "인사이트", 36000, "04"),
                createDto("삼국지", "민음사", 15000, "02"),
                createDto("축구의 역사", "한빛미디어", 22000, "03")
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
                createDto("테스트책", "출판사", 10000, "01")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(999999L, dtos))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("실패 - 소유주가 해당 책장을 점유하고 있지 않음")
    void registerBooks_notOccupiedByOwner() {
        // 새 책장 생성 (점유 안 함)
        BookCaseCreateDto newDto = new BookCaseCreateDto();
        newDto.setLocationCode("02");
        newDto.setBookCaseTypeId(typeId);
        long unoccupiedCaseId = bookCaseService.create(newDto);

        List<BookRegisterDto> dtos = List.of(
                createDto("테스트책", "출판사", 10000, "01")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(unoccupiedCaseId, dtos))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("실패 - 다른 소유주가 점유 중인 책장에 등록 시도")
    void registerBooks_occupiedByDifferentOwner() {
        // 다른 소유주 생성 + 새 책장 점유
        String otherCode = uniqueCode();
        BookOwnerVO otherOwner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("김철수")
                .email("other-" + otherCode + "@test.com")
                .phone("010-" + otherCode.substring(0, 4) + "-" + otherCode.substring(4))
                .password("password123")
                .residentNumber(otherCode + "-1234567")
                .bankName("신한은행")
                .accountNumber("999-888-777")
                .build());

        BookCaseCreateDto otherDto = new BookCaseCreateDto();
        otherDto.setLocationCode("03");
        otherDto.setBookCaseTypeId(typeId);
        long otherCaseId = bookCaseService.create(otherDto);
        bookCaseService.occupy(otherOwner.getId(), List.of(otherCaseId), LocalDate.now().plusMonths(3), 50000);

        // 홍길동 이름으로 김철수의 책장에 등록 시도
        List<BookRegisterDto> dtos = List.of(
                createDto("테스트책", "출판사", 10000, "01")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(otherCaseId, dtos))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("실패 - 소유주 정보가 리스트 내에서 불일치")
    void registerBooks_ownerMismatch() {
        BookRegisterDto dto1 = createDto("책1", "출판사", 10000, "01");
        BookRegisterDto dto2 = createDto("책2", "출판사", 15000, "02");
        dto2.setUserName("다른사람");

        List<BookRegisterDto> dtos = List.of(dto1, dto2);

        assertThatThrownBy(() -> bookCaseService.registerBooks(bookCaseId, dtos))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 책 분류")
    void registerBooks_invalidBookType() {
        List<BookRegisterDto> dtos = List.of(
                createDto("테스트책", "출판사", 10000, "99")
        );

        assertThatThrownBy(() -> bookCaseService.registerBooks(bookCaseId, dtos))
                .isInstanceOf(ApplicationException.class);
    }
}

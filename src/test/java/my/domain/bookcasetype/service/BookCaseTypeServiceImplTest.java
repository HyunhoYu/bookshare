package my.domain.bookcasetype.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.BookCaseTypeVO;
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
class BookCaseTypeServiceImplTest {

    @Autowired
    private BookCaseTypeService bookCaseTypeService;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private BookCaseTypeCreateDto createDto(String code, int monthlyPrice) {
        BookCaseTypeCreateDto dto = new BookCaseTypeCreateDto();
        dto.setCode(code);
        dto.setMonthlyPrice(monthlyPrice);
        return dto;
    }

    @Test
    @DisplayName("책장 타입 저장 성공 - ID 반환")
    void addBookCaseType_success() {
        // given
        BookCaseTypeCreateDto dto = createDto(uniqueCode(), 50000);

        // when
        long id = bookCaseTypeService.create(dto);

        // then
        assertThat(id).isGreaterThan(0);
    }

    @Test
    @DisplayName("책장 타입 저장 후 ID로 조회")
    void addBookCaseType_thenFindById() {
        // given
        String code = uniqueCode();
        BookCaseTypeCreateDto dto = createDto(code, 50000);
        long id = bookCaseTypeService.create(dto);

        // when
        BookCaseTypeVO result = bookCaseTypeService.findById(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getMonthlyPrice()).isEqualTo(50000);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("책장 타입 여러 건 저장 후 전체 조회")
    void addMultiple_thenFindAll() {
        // given
        bookCaseTypeService.create(createDto(uniqueCode(), 50000));
        bookCaseTypeService.create(createDto(uniqueCode(), 30000));

        // when
        List<BookCaseTypeVO> list = bookCaseTypeService.findAll();

        // then
        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("같은 코드로 저장 시 중복 예외 발생")
    void addSameCodeTwice_throwsDuplicateException() {
        // given
        String code = uniqueCode();
        bookCaseTypeService.create(createDto(code, 50000));

        // when & then
        assertThatThrownBy(() -> bookCaseTypeService.create(createDto(code, 30000)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_BOOK_CASE_TYPE_CODE));
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 null 반환")
    void findById_notFound() {
        // when
        BookCaseTypeVO result = bookCaseTypeService.findById(999999L);

        // then
        assertThat(result).isNull();
    }
}

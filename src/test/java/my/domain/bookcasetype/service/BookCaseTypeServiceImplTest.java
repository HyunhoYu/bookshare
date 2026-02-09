package my.domain.bookcasetype.service;

import my.domain.bookcasetype.BookCaseTypeVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookCaseTypeServiceImplTest {

    @Autowired
    private BookCaseTypeService bookCaseTypeService;

    private BookCaseTypeVO createVO(String code, int monthlyPrice) {
        BookCaseTypeVO vo = new BookCaseTypeVO();
        vo.setCode(code);
        vo.setMonthlyPrice(monthlyPrice);
        return vo;
    }

    @Test
    @DisplayName("책장 타입 저장 성공 - ID 반환")
    void addBookCaseType_success() {
        // given
        BookCaseTypeVO vo = createVO("01", 50000);

        // when
        long id = bookCaseTypeService.addBookCaseType(vo);

        // then
        assertThat(id).isGreaterThan(0);
    }

    @Test
    @DisplayName("책장 타입 저장 후 ID로 조회")
    void addBookCaseType_thenFindById() {
        // given
        BookCaseTypeVO vo = createVO("01", 50000);
        long id = bookCaseTypeService.addBookCaseType(vo);

        // when
        BookCaseTypeVO result = bookCaseTypeService.findById(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCode()).isEqualTo("01");
        assertThat(result.getMonthlyPrice()).isEqualTo(50000);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("책장 타입 여러 건 저장 후 전체 조회")
    void addMultiple_thenFindAll() {
        // given
        bookCaseTypeService.addBookCaseType(createVO("01", 50000));
        bookCaseTypeService.addBookCaseType(createVO("02", 30000));

        // when
        List<BookCaseTypeVO> list = bookCaseTypeService.findAll();

        // then
        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("같은 코드로 여러 번 저장 - 각각 다른 ID 부여")
    void addSameCodeTwice_differentIds() {
        // given
        long id1 = bookCaseTypeService.addBookCaseType(createVO("01", 50000));
        long id2 = bookCaseTypeService.addBookCaseType(createVO("01", 50000));

        // then
        assertThat(id1).isNotEqualTo(id2);
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

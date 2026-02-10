package my.domain.bookcase;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookCaseMapper {

    int insertBookCase(BookCaseVO bookCaseVO);
    BookCaseVO selectById(long id);
    List<BookCaseVO> selectAll();
    List<BookCaseVO> selectUsableBookCases();
    List<Long> selectMyOccupyingBookCasesByBookOwnerId(Long bookOwnerId);
}

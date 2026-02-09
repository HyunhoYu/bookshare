package my.domain.bookcasetype;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookCaseTypeMapper {

    int insertBookCaseType(BookCaseTypeVO bookCaseTypeVO);
    BookCaseTypeVO selectById(long id);
    List<BookCaseTypeVO> selectAll();
}

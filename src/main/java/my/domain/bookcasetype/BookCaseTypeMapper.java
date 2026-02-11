package my.domain.bookcasetype;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookCaseTypeMapper {

    int insert(BookCaseTypeVO bookCaseTypeVO);
    BookCaseTypeVO selectById(long id);
    BookCaseTypeVO selectByCode(String code);
    List<BookCaseTypeVO> selectAll();
}

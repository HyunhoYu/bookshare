package my.domain.book;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper {

    List<BookVO> selectAll();
    List<BookVO> selectBooksByBookOwnerId(Long id);
    List<BookVO> selectSoldBookByBookOwnerId(Long id);
    int insertBook(BookVO bookVO);
    BookVO selectById(Long id);
    int updateStateSold(Long id);

}

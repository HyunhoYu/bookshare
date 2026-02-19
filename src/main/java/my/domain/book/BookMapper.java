package my.domain.book;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookMapper {

    List<BookVO> selectAll();
    List<BookVO> selectBooksByBookOwnerId(Long id);
    List<BookVO> selectSoldBookByBookOwnerId(Long id);
    int insert(BookVO bookVO);
    BookVO selectById(Long id);
    BookVO selectByIdIncludeDeleted(Long id);
    int updateStateSold(Long id);
    List<Long> selectNormalBookIdsByBookCaseIds(@Param("bookCaseIds") List<Long> bookCaseIds);
    int updateStateNormalToRetrieve(List<Long>bookIds);
    int softDeleteBooks(@Param("bookIds") List<Long> bookIds);
    int countByIdsAndState(@Param("bookIds") List<Long> bookIds, @Param("state") String state);
    List<BookWithBookCaseVO> selectAllWithBookCase(@Param("state") String state);
    List<BookVO> selectByBookCaseId(Long bookCaseId);

}

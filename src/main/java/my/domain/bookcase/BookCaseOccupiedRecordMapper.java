package my.domain.bookcase;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookCaseOccupiedRecordMapper {

    int insert(BookCaseOccupiedRecordVO record);
    BookCaseOccupiedRecordVO selectById(long id);
    BookCaseOccupiedRecordVO selectCurrentByBookCaseId(long bookCaseId);
    List<BookCaseOccupiedRecordVO> selectByBookOwnerId(long bookOwnerId);
    int updateUnOccupiedAt(long id);
    int countCurrentOccupied(long bookCaseId);
    int unOccupyBookCases(@Param("bookCaseIds") List<Long> bookCaseIds);
    List<BookCaseOccupiedRecordVO> selectOccupiedRecordByIds(@Param("occupiedRecordIds") List<Long> occupiedRecordIds);
    List<BookCaseVO> selectBookCasesByOwnerId(long id);
}

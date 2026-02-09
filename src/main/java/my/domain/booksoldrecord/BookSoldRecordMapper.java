package my.domain.booksoldrecord;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import my.domain.booksoldrecord.vo.BookSoldRecordVO;

@Mapper
public interface BookSoldRecordMapper {

    List<BookSoldRecordVO> selectAll();
    BookSoldRecordVO selectById(Long id);
    List<BookSoldRecordVO> selectByCustomerId(Long customerId);
    List<BookSoldRecordVO> selectBySettlementId(Long settlementId);
    void insert(BookSoldRecordVO bookSoldRecordVO);
}

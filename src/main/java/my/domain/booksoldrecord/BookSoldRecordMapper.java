package my.domain.booksoldrecord;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import my.domain.booksoldrecord.vo.BookSoldRecordVO;

@Mapper
public interface BookSoldRecordMapper {

    List<BookSoldRecordVO> selectAll();
    BookSoldRecordVO selectById(Long id);
    List<BookSoldRecordVO> selectByCustomerId(Long customerId);
    List<BookSoldRecordVO> selectBySettlementId(Long settlementId);
    int insert(BookSoldRecordVO bookSoldRecordVO);
    List<BookSoldRecordVO> selectUnsettled();
    List<BookSoldRecordVO> selectUnsettledByBookOwnerId(Long bookOwnerId);
    int countByIdsAndBookOwnerId(@Param("ids") List<Long> ids, @Param("bookOwnerId") Long bookOwnerId);
    int countAlreadySettled(@Param("ids") List<Long> ids);
    int updateSettlementId(@Param("settlementId") Long settlementId, @Param("ids") List<Long> ids);
    Map<String, Object> sumAmountsByIds(@Param("ids") List<Long> ids);
    List<Long> selectUnsettledBookOwnerIds();
    int countUnsettledByBookCaseId(Long bookCaseId);
}

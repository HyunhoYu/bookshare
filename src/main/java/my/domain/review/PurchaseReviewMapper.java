package my.domain.review;

import my.domain.review.dto.ReviewSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PurchaseReviewMapper {
    int insert(PurchaseReviewVO vo);
    int update(PurchaseReviewVO vo);
    int delete(Long id);
    PurchaseReviewVO selectById(Long id);
    PurchaseReviewVO selectByBookSaleRecordId(Long bookSaleRecordId);
    List<PurchaseReviewVO> selectByCustomerId(Long customerId);
    List<PurchaseReviewVO> selectByBookOwnerId(Long bookOwnerId);
    ReviewSummaryDto selectSummaryByBookOwnerId(Long bookOwnerId);
}

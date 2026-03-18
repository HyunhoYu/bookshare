package my.domain.review.service;

import my.domain.review.PurchaseReviewVO;
import my.domain.review.dto.ReviewCreateDto;
import my.domain.review.dto.ReviewSummaryDto;
import my.domain.review.dto.ReviewUpdateDto;
import java.util.List;

public interface PurchaseReviewService {
    PurchaseReviewVO create(Long customerId, ReviewCreateDto dto);
    PurchaseReviewVO update(Long reviewId, Long customerId, ReviewUpdateDto dto);
    void delete(Long reviewId, Long customerId);
    List<PurchaseReviewVO> findByCustomerId(Long customerId);
    List<PurchaseReviewVO> findByBookOwnerId(Long bookOwnerId);
    ReviewSummaryDto getSummary(Long bookOwnerId);
}

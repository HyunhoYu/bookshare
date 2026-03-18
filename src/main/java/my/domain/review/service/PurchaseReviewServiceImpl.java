package my.domain.review.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.notification.service.NotificationService;
import my.domain.review.PurchaseReviewMapper;
import my.domain.review.PurchaseReviewVO;
import my.domain.review.dto.ReviewCreateDto;
import my.domain.review.dto.ReviewSummaryDto;
import my.domain.review.dto.ReviewUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseReviewServiceImpl implements PurchaseReviewService {

    private final PurchaseReviewMapper reviewMapper;
    private final BookSoldRecordMapper saleRecordMapper;
    private final BookMapper bookMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PurchaseReviewVO create(Long customerId, ReviewCreateDto dto) {
        BookSoldRecordVO saleRecord = requireNonNull(
                saleRecordMapper.selectById(dto.getBookSaleRecordId()), ErrorCode.BOOK_NOT_FOUND);

        if (!saleRecord.getCustomerId().equals(customerId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        if (reviewMapper.selectByBookSaleRecordId(dto.getBookSaleRecordId()) != null) {
            throw new ApplicationException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        BookVO book = bookMapper.selectByIdIncludeDeleted(dto.getBookSaleRecordId());
        Long bookOwnerId = book != null ? book.getBookOwnerId() : null;

        PurchaseReviewVO vo = new PurchaseReviewVO();
        vo.setBookSaleRecordId(dto.getBookSaleRecordId());
        vo.setCustomerId(customerId);
        vo.setBookOwnerId(bookOwnerId);
        vo.setRating(dto.getRating());
        vo.setContent(dto.getContent());

        int result = reviewMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.REVIEW_INSERT_FAIL);
        }

        // BookOwner에게 알림
        if (bookOwnerId != null) {
            String stars = "★".repeat(dto.getRating()) + "☆".repeat(5 - dto.getRating());
            String bookName = book.getBookName();
            notificationService.create(
                    bookOwnerId,
                    "NEW_REVIEW",
                    "새 리뷰가 등록되었습니다. " + stars + " \"" + bookName + "\"",
                    vo.getId()
            );
        }

        return reviewMapper.selectById(vo.getId());
    }

    @Override
    @Transactional
    public PurchaseReviewVO update(Long reviewId, Long customerId, ReviewUpdateDto dto) {
        PurchaseReviewVO existing = requireNonNull(reviewMapper.selectById(reviewId), ErrorCode.REVIEW_NOT_FOUND);
        if (!existing.getCustomerId().equals(customerId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        existing.setRating(dto.getRating());
        existing.setContent(dto.getContent());
        reviewMapper.update(existing);

        return reviewMapper.selectById(reviewId);
    }

    @Override
    @Transactional
    public void delete(Long reviewId, Long customerId) {
        PurchaseReviewVO existing = requireNonNull(reviewMapper.selectById(reviewId), ErrorCode.REVIEW_NOT_FOUND);
        if (!existing.getCustomerId().equals(customerId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }
        reviewMapper.delete(reviewId);
    }

    @Override
    public List<PurchaseReviewVO> findByCustomerId(Long customerId) {
        return reviewMapper.selectByCustomerId(customerId);
    }

    @Override
    public List<PurchaseReviewVO> findByBookOwnerId(Long bookOwnerId) {
        return reviewMapper.selectByBookOwnerId(bookOwnerId);
    }

    @Override
    public ReviewSummaryDto getSummary(Long bookOwnerId) {
        return reviewMapper.selectSummaryByBookOwnerId(bookOwnerId);
    }
}

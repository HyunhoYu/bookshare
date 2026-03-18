package my.api.review;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.review.PurchaseReviewVO;
import my.domain.review.dto.ReviewCreateDto;
import my.domain.review.dto.ReviewSummaryDto;
import my.domain.review.dto.ReviewUpdateDto;
import my.domain.review.service.PurchaseReviewService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class PurchaseReviewController {

    private final PurchaseReviewService reviewService;

    @RequireRole(Role.CUSTOMER)
    @PostMapping
    public ApiResponse<PurchaseReviewVO> create(
            HttpServletRequest request,
            @RequestBody @Valid ReviewCreateDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.created(reviewService.create(userId, dto));
    }

    @RequireRole(Role.CUSTOMER)
    @PutMapping("/{id}")
    public ApiResponse<PurchaseReviewVO> update(
            HttpServletRequest request,
            @PathVariable("id") Long id,
            @RequestBody @Valid ReviewUpdateDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.update(id, userId, dto));
    }

    @RequireRole(Role.CUSTOMER)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            HttpServletRequest request,
            @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute("userId");
        reviewService.delete(id, userId);
        return ApiResponse.success(null);
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping("/my")
    public ApiResponse<List<PurchaseReviewVO>> findMyReviews(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.findByCustomerId(userId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/book-owner/{id}")
    public ApiResponse<List<PurchaseReviewVO>> findByBookOwner(@PathVariable("id") Long bookOwnerId) {
        return ApiResponse.success(reviewService.findByBookOwnerId(bookOwnerId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/book-owner/{id}/summary")
    public ApiResponse<ReviewSummaryDto> getSummary(@PathVariable("id") Long bookOwnerId) {
        return ApiResponse.success(reviewService.getSummary(bookOwnerId));
    }
}

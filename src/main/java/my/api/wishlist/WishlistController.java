package my.api.wishlist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.wishlist.WishlistVO;
import my.domain.wishlist.service.WishlistService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @RequireRole(Role.CUSTOMER)
    @PostMapping("/{bookId}")
    public ApiResponse<?> addWishlist(HttpServletRequest request, @PathVariable Long bookId) {
        Long userId = (Long) request.getAttribute("userId");
        wishlistService.addWishlist(userId, bookId);
        return ApiResponse.created("관심 도서 등록 완료");
    }

    @RequireRole(Role.CUSTOMER)
    @DeleteMapping("/{bookId}")
    public ApiResponse<?> removeWishlist(HttpServletRequest request, @PathVariable Long bookId) {
        Long userId = (Long) request.getAttribute("userId");
        wishlistService.removeWishlist(userId, bookId);
        return ApiResponse.success("관심 도서 해제 완료", null);
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping("/my")
    public ApiResponse<List<WishlistVO>> getMyWishlist(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(wishlistService.getMyWishlist(userId));
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping("/check/{bookId}")
    public ApiResponse<Boolean> checkWishlist(HttpServletRequest request, @PathVariable Long bookId) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(wishlistService.checkWishlist(userId, bookId));
    }
}

package my.api.post_like;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.post_like.service.PostLikeService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @PostMapping
    public ApiResponse<Void> like(
            HttpServletRequest request,
            @PathVariable("postId") Long postId) {
        Long userId = (Long) request.getAttribute("userId");
        postLikeService.like(postId, userId);
        return ApiResponse.created(null);
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @DeleteMapping
    public ApiResponse<Void> unlike(
            HttpServletRequest request,
            @PathVariable("postId") Long postId) {
        Long userId = (Long) request.getAttribute("userId");
        postLikeService.unlike(postId, userId);
        return ApiResponse.success(null);
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/check")
    public ApiResponse<Boolean> isLiked(
            HttpServletRequest request,
            @PathVariable("postId") Long postId) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(postLikeService.isLiked(postId, userId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/count")
    public ApiResponse<Integer> getLikeCount(@PathVariable("postId") Long postId) {
        return ApiResponse.success(postLikeService.getLikeCount(postId));
    }
}

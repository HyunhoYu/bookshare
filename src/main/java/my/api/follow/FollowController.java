package my.api.follow;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.bookowner_post.service.BookOwnerPostService;
import my.domain.follow.FollowVO;
import my.domain.follow.service.FollowService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;
    private final BookOwnerPostService postService;

    @RequireRole(Role.CUSTOMER)
    @PostMapping("/{bookOwnerId}")
    public ApiResponse<FollowVO> follow(
            HttpServletRequest request,
            @PathVariable("bookOwnerId") Long bookOwnerId) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.created(followService.follow(customerId, bookOwnerId));
    }

    @RequireRole(Role.CUSTOMER)
    @DeleteMapping("/{bookOwnerId}")
    public ApiResponse<Void> unfollow(
            HttpServletRequest request,
            @PathVariable("bookOwnerId") Long bookOwnerId) {
        Long customerId = (Long) request.getAttribute("userId");
        followService.unfollow(customerId, bookOwnerId);
        return ApiResponse.success(null);
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping
    public ApiResponse<List<FollowVO>> getFollowList(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.success(followService.getFollowList(customerId));
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping("/feed")
    public ApiResponse<List<BookOwnerPostVO>> getFeed(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.success(postService.findFeed(customerId));
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping("/check/{bookOwnerId}")
    public ApiResponse<Boolean> isFollowing(
            HttpServletRequest request,
            @PathVariable("bookOwnerId") Long bookOwnerId) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.success(followService.isFollowing(customerId, bookOwnerId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/count/{bookOwnerId}")
    public ApiResponse<Integer> getFollowerCount(@PathVariable("bookOwnerId") Long bookOwnerId) {
        return ApiResponse.success(followService.getFollowerCount(bookOwnerId));
    }

    @RequireRole(Role.BOOK_OWNER)
    @GetMapping("/my-followers")
    public ApiResponse<List<FollowVO>> getMyFollowers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(followService.getFollowers(userId));
    }
}

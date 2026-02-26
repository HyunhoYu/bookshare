package my.api.post_comment;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.post_comment.PostCommentVO;
import my.domain.post_comment.dto.PostCommentCreateDto;
import my.domain.post_comment.dto.PostCommentUpdateDto;
import my.domain.post_comment.service.PostCommentService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class PostCommentController {

    private final PostCommentService commentService;

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<PostCommentVO>> findByPostId(@PathVariable("postId") Long postId) {
        return ApiResponse.success(commentService.findByPostId(postId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @PostMapping
    public ApiResponse<PostCommentVO> create(
            HttpServletRequest request,
            @PathVariable("postId") Long postId,
            @RequestBody @Valid PostCommentCreateDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.created(commentService.create(postId, userId, dto));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @PutMapping("/{commentId}")
    public ApiResponse<PostCommentVO> update(
            HttpServletRequest request,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody @Valid PostCommentUpdateDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(commentService.update(commentId, userId, dto));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(
            HttpServletRequest request,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {
        Long userId = (Long) request.getAttribute("userId");
        commentService.delete(commentId, userId);
        return ApiResponse.success(null);
    }
}

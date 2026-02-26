package my.api.bookowner_post;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.bookowner_post.dto.BookOwnerPostCreateDto;
import my.domain.bookowner_post.dto.BookOwnerPostUpdateDto;
import my.domain.bookowner_post.service.BookOwnerPostService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class BookOwnerPostController {

    private final BookOwnerPostService postService;

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<BookOwnerPostVO>> findAll() {
        return ApiResponse.success(postService.findAll());
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/{id}")
    public ApiResponse<BookOwnerPostVO> findById(@PathVariable("id") Long id) {
        BookOwnerPostVO post = postService.findById(id);
        if (post == null) {
            return ApiResponse.notFound("존재하지 않는 게시글입니다");
        }
        return ApiResponse.success(post);
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/book-owner/{bookOwnerId}")
    public ApiResponse<List<BookOwnerPostVO>> findByBookOwner(@PathVariable("bookOwnerId") Long bookOwnerId) {
        return ApiResponse.success(postService.findByBookOwnerId(bookOwnerId));
    }

    @RequireRole(Role.BOOK_OWNER)
    @PostMapping
    public ApiResponse<BookOwnerPostVO> create(
            HttpServletRequest request,
            @RequestBody @Valid BookOwnerPostCreateDto dto) {
        Long bookOwnerId = (Long) request.getAttribute("userId");
        return ApiResponse.created(postService.create(bookOwnerId, dto));
    }

    @RequireRole(Role.BOOK_OWNER)
    @PutMapping("/{id}")
    public ApiResponse<BookOwnerPostVO> update(
            HttpServletRequest request,
            @PathVariable("id") Long id,
            @RequestBody @Valid BookOwnerPostUpdateDto dto) {
        Long bookOwnerId = (Long) request.getAttribute("userId");
        return ApiResponse.success(postService.update(id, bookOwnerId, dto));
    }

    @RequireRole(Role.BOOK_OWNER)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            HttpServletRequest request,
            @PathVariable("id") Long id) {
        Long bookOwnerId = (Long) request.getAttribute("userId");
        postService.delete(id, bookOwnerId);
        return ApiResponse.success(null);
    }
}

package my.api.bookowner_profile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.bookowner_profile.BookOwnerProfileVO;
import my.domain.bookowner_profile.dto.BookOwnerProfileRequestDto;
import my.domain.bookowner_profile.service.BookOwnerProfileService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-owners/{bookOwnerId}/profile")
@RequiredArgsConstructor
public class BookOwnerProfileController {

    private final BookOwnerProfileService profileService;

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<BookOwnerProfileVO> getProfile(@PathVariable("bookOwnerId") Long bookOwnerId) {
        BookOwnerProfileVO profile = profileService.findByBookOwnerId(bookOwnerId);
        if (profile == null) {
            return ApiResponse.notFound("프로필이 존재하지 않습니다");
        }
        return ApiResponse.success(profile);
    }

    @RequireRole(Role.BOOK_OWNER)
    @PostMapping
    public ApiResponse<BookOwnerProfileVO> createProfile(
            HttpServletRequest request,
            @PathVariable("bookOwnerId") Long bookOwnerId,
            @RequestBody @Valid BookOwnerProfileRequestDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (!userId.equals(bookOwnerId)) {
            return ApiResponse.error(403, "본인의 프로필만 생성할 수 있습니다");
        }
        return ApiResponse.created(profileService.create(bookOwnerId, dto));
    }

    @RequireRole(Role.BOOK_OWNER)
    @PutMapping
    public ApiResponse<BookOwnerProfileVO> updateProfile(
            HttpServletRequest request,
            @PathVariable("bookOwnerId") Long bookOwnerId,
            @RequestBody @Valid BookOwnerProfileRequestDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (!userId.equals(bookOwnerId)) {
            return ApiResponse.error(403, "본인의 프로필만 수정할 수 있습니다");
        }
        return ApiResponse.success(profileService.update(bookOwnerId, dto));
    }
}

package my.api.announcement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.announcement.AnnouncementVO;
import my.domain.announcement.dto.AnnouncementCreateDto;
import my.domain.announcement.dto.AnnouncementUpdateDto;
import my.domain.announcement.service.AnnouncementService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @RequireRole(Role.ADMIN)
    @PostMapping
    public ApiResponse<AnnouncementVO> create(HttpServletRequest request,
                                               @Valid @RequestBody AnnouncementCreateDto dto) {
        Long adminId = (Long) request.getAttribute("userId");
        return ApiResponse.created(announcementService.create(adminId, dto));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<AnnouncementVO>> getAll(HttpServletRequest request) {
        String role = (String) request.getAttribute("userRole");
        if ("ADMIN".equals(role)) {
            return ApiResponse.success(announcementService.getAll());
        }
        return ApiResponse.success(announcementService.getByRole(role));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/{id}")
    public ApiResponse<AnnouncementVO> getById(@PathVariable Long id) {
        return ApiResponse.success(announcementService.getById(id));
    }

    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<AnnouncementVO> update(@PathVariable Long id,
                                               @Valid @RequestBody AnnouncementUpdateDto dto) {
        return ApiResponse.success("공지사항 수정 완료", announcementService.update(id, dto));
    }

    @RequireRole(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.success("공지사항 삭제 완료", null);
    }
}

package my.api.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.notification.NotificationVO;
import my.domain.notification.service.NotificationService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<NotificationVO>> findMyNotifications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(notificationService.findByUserId(userId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            HttpServletRequest request,
            @PathVariable("id") Long id) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return ApiResponse.success(null);
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(notificationService.countUnread(userId));
    }
}

package my.api.book_request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book_request.BookRequestVO;
import my.domain.book_request.dto.BookRequestCreateDto;
import my.domain.book_request.dto.BookRequestStatusUpdateDto;
import my.domain.book_request.service.BookRequestService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-requests")
public class BookRequestController {

    private final BookRequestService bookRequestService;

    @RequireRole(Role.CUSTOMER)
    @PostMapping
    public ApiResponse<BookRequestVO> create(
            HttpServletRequest request,
            @RequestBody @Valid BookRequestCreateDto dto) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.created(bookRequestService.create(customerId, dto));
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping("/my")
    public ApiResponse<List<BookRequestVO>> findMyRequests(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.success(bookRequestService.findByCustomerId(customerId));
    }

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<BookRequestVO>> findAll() {
        return ApiResponse.success(bookRequestService.findAll());
    }

    @RequireRole({Role.ADMIN, Role.CUSTOMER})
    @GetMapping("/{id}")
    public ApiResponse<BookRequestVO> findById(@PathVariable("id") Long id) {
        BookRequestVO bookRequest = bookRequestService.findById(id);
        if (bookRequest == null) {
            return ApiResponse.notFound("존재하지 않는 입고 요청입니다");
        }
        return ApiResponse.success(bookRequest);
    }

    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}/status")
    public ApiResponse<BookRequestVO> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody @Valid BookRequestStatusUpdateDto dto) {
        return ApiResponse.success(bookRequestService.updateStatus(id, dto));
    }
}

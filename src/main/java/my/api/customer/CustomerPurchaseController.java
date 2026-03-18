package my.api.customer;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.booksoldrecord.service.BookSoldRecordService;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.enums.Role;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/purchases")
public class CustomerPurchaseController {

    private final BookSoldRecordService bookSoldRecordService;

    @RequireRole({Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<BookSoldRecordVO>> getMyPurchases(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(bookSoldRecordService.findByCustomerId(userId));
    }
}

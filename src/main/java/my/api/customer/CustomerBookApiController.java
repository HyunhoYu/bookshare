package my.api.customer;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book.BookService;
import my.domain.book.CustomerBookDetailVO;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.enums.Role;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/books")
public class CustomerBookApiController {

    private final BookService bookService;
    private final BookSoldRecordMapper bookSoldRecordMapper;

    @RequireRole({Role.ADMIN, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<CustomerBookDetailVO>> findAll(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "genre", required = false) String genreCode,
            @RequestParam(value = "location", required = false) String locationCode,
            @RequestParam(value = "bookOwnerId", required = false) Long bookOwnerId) {
        return ApiResponse.success(bookService.findForCustomerBrowse(search, genreCode, locationCode, bookOwnerId));
    }

    @RequireRole({Role.ADMIN, Role.CUSTOMER})
    @GetMapping("/{id}")
    public ApiResponse<CustomerBookDetailVO> findById(@PathVariable("id") Long id) {
        return ApiResponse.success(bookService.findCustomerBookDetail(id));
    }

    @RequireRole({Role.ADMIN, Role.CUSTOMER})
    @GetMapping("/my-purchases")
    public ApiResponse<List<BookSoldRecordVO>> findMyPurchases(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(bookSoldRecordMapper.selectDetailByCustomerId(userId));
    }
}

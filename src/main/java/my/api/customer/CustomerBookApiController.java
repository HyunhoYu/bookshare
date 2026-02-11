package my.api.customer;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book.BookService;
import my.domain.book.BookVO;
import my.enums.Role;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/books")
public class CustomerBookApiController {

    private final BookService bookService;

    @RequireRole({Role.ADMIN, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<BookVO>> findAll() {
        return ApiResponse.success(bookService.findAll());
    }
}

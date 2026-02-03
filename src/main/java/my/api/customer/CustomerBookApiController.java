package my.api.customer;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import my.common.response.ApiResponse;
import my.domain.book.BookService;
import my.domain.book.BookVO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/books")
public class CustomerBookApiController {

    private final BookService bookService;

    @GetMapping
    public ApiResponse<List<BookVO>> getAllBooks() {
        return ApiResponse.success(bookService.findAll());
    }
}

package my.api.book;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book.BookService;
import my.domain.book.BookWithBookCaseVO;
import my.enums.Role;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping
    public ApiResponse<List<BookWithBookCaseVO>> findAllWithBookCase(@RequestParam(required = false) String state) {
        List<BookWithBookCaseVO> result = bookService.findAllWithBookCase(state);
        return ApiResponse.success(result);
    }



    @RequireRole(Role.ADMIN)
    @PostMapping("/retrieve")
    public ApiResponse<List<Long>> retrieveBooks(@RequestBody List<Long> bookIds) {
        List<Long> result = bookService.retrieveBooks(bookIds);
        return ApiResponse.success(result);
    }

}

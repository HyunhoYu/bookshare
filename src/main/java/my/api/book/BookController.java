package my.api.book;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book.BookService;
import my.enums.Role;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @RequireRole(Role.ADMIN)
    @PostMapping("/retrieve")
    public ApiResponse<List<Long>> retrieveBooks(@RequestBody List<Long> bookIds) {
        List<Long> result = bookService.retrieveBooks(bookIds);
        return ApiResponse.success(result);
    }

}

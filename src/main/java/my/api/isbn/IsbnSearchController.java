package my.api.isbn;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book_request.dto.BookSearchResultDto;
import my.domain.book_request.service.BookSearchService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class IsbnSearchController {

    private final BookSearchService bookSearchService;

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER, Role.CUSTOMER})
    @GetMapping("/search")
    public ApiResponse<List<BookSearchResultDto>> search(@RequestParam("query") String query) {
        return ApiResponse.success(bookSearchService.search(query));
    }
}

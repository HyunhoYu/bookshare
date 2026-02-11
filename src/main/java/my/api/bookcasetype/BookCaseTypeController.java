package my.api.bookcasetype;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.BookCaseTypeVO;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.enums.Role;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-case-types")
public class BookCaseTypeController {

    private final BookCaseTypeService bookCaseTypeService;

    @RequireRole(Role.ADMIN)
    @PostMapping
    public ApiResponse<BookCaseTypeVO> create(@RequestBody @Valid BookCaseTypeCreateDto dto) {

        long id = bookCaseTypeService.create(dto);
        BookCaseTypeVO result = bookCaseTypeService.findById(id);
        return ApiResponse.created(result);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<BookCaseTypeVO>> findAll() {
        return ApiResponse.success(bookCaseTypeService.findAll());
    }
}

package my.api.bookcase;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book.BookService;
import my.domain.book.BookVO;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.BookCaseWithOccupationVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcase.OccupyRequestDto;
import my.domain.bookcase.service.BookCaseService;
import my.enums.Role;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-cases")
public class BookCaseController {

    private final BookCaseService bookCaseService;
    private final BookService bookService;

    @RequireRole(Role.ADMIN)
    @PostMapping
    public ApiResponse<BookCaseVO> create(@RequestBody @Valid BookCaseCreateDto dto) {
        long id = bookCaseService.create(dto);
        BookCaseVO result = bookCaseService.findById(id);
        return ApiResponse.created(result);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<BookCaseWithOccupationVO>> findAll() {
        return ApiResponse.success(bookCaseService.findAllWithOccupation());
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/{book-case-id}")
    public ApiResponse<BookCaseVO> findById(@PathVariable("book-case-id") Long bookCaseId) {
        return ApiResponse.success(bookCaseService.findById(bookCaseId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER})
    @GetMapping("/usable")
    public ApiResponse<List<BookCaseVO>> findUsable() {
        List<BookCaseVO> result = bookCaseService.findUsable();

        return ApiResponse.success(result);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/occupy")
    public ApiResponse<List<BookCaseOccupiedRecordVO>> occupy(@RequestBody @Valid OccupyRequestDto dto) {
        List<BookCaseOccupiedRecordVO> result = bookCaseService.occupy(dto.getBookOwnerId(), dto.getBookCaseIds(), dto.getExpirationDate());
        return ApiResponse.created(result);
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER})
    @GetMapping("/{book-case-id}/books")
    public ApiResponse<List<BookVO>> findBooksByBookCaseId(@PathVariable("book-case-id") Long bookCaseId) {
        List<BookVO> result = bookService.findByBookCaseId(bookCaseId);
        return ApiResponse.success(result);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/{book-case-id}/books")
    public ApiResponse<List<BookVO>> registerBooks(@PathVariable("book-case-id") Long bookCaseId, @RequestBody @Valid List<BookRegisterDto> bookRegisterDtos) {
        List<BookVO> result = bookCaseService.registerBooks(bookCaseId, bookRegisterDtos);
        return ApiResponse.created(result);
    }


    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{bookowner-id}")
    public ApiResponse<List<Long>> findByBookOwnerId(@PathVariable("bookowner-id") Long bookOwnerId) {
        List<Long> ids = bookCaseService.findOccupyingBookCaseIds(bookOwnerId);
        return ApiResponse.success(ids);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/unoccupy")
    public ApiResponse<List<Long>> unOccupy(@RequestBody List<Long> bookCaseIds) {
        List<Long> changedBookIds = bookCaseService.unOccupyProcess(bookCaseIds);
        return ApiResponse.success(changedBookIds);
    }

}

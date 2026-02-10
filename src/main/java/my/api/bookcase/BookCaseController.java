package my.api.bookcase;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book.BookVO;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.BookRegisterDto;
import my.domain.bookcase.service.BookCaseService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-cases")
public class BookCaseController {

    private final BookCaseService bookCaseService;

    @RequireRole(Role.ADMIN)
    @PostMapping
    public ApiResponse<BookCaseVO> addBookCase(@RequestBody BookCaseVO bookCaseVO) {
        long id = bookCaseService.addBookCase(bookCaseVO);
        BookCaseVO result = bookCaseService.findById(id);
        return ApiResponse.created(result);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<BookCaseVO>> findAll() {
        return ApiResponse.success(bookCaseService.findAll());
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/{book-case-id}")
    public ApiResponse<BookCaseVO> findById(@PathVariable("book-case-id") long bookCaseId) {
        return ApiResponse.success(bookCaseService.findById(bookCaseId));
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER})
    @GetMapping("/usable")
    public ApiResponse<List<BookCaseVO>> findUsableBookCases() {
        List<BookCaseVO> result = bookCaseService.findUsableBookCases();

        return ApiResponse.success(result);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/{book-case-id}/occupy")
    public ApiResponse<BookCaseOccupiedRecordVO> occupy(@PathVariable("book-case-id") long bookCaseId, HttpServletRequest request) {
        long bookOwnerId = (long) request.getAttribute("userId");
        BookCaseOccupiedRecordVO result = bookCaseService.occupy(bookOwnerId, bookCaseId);
        return ApiResponse.created(result);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/{book-case-id}/books")
    public ApiResponse<List<BookVO>> registerBooks(@PathVariable("book-case-id") long bookCaseId, @RequestBody List<BookRegisterDto> bookRegisterDtos) {
        List<BookVO> result = bookCaseService.registerBooks(bookCaseId, bookRegisterDtos);
        return ApiResponse.created(result);
    }


    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{bookowner-id}")
    public ApiResponse<List<Long>> findMyBookCases(@PathVariable("bookowner-id") long bookOwnerId) {
        List<Long> ids = bookCaseService.selectMyOccupyingBookCasesByBookOwnerId(bookOwnerId);
        return ApiResponse.success(ids);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/unoccupy")
    public ApiResponse<List<Long>> unOccupyAndChangeBookState(@RequestBody List<Long> bookCaseIds) {
        List<Long> changedBookIds = bookCaseService.unOccupyProcess(bookCaseIds);
        return ApiResponse.success(changedBookIds);
    }










}

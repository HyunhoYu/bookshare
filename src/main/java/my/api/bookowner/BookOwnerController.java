package my.api.bookowner;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.book.BookVO;
import my.domain.bookowner.service.BookOwnerService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.settlement.vo.SettlementVO;
import my.enums.Role;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/book-owners")
@RequiredArgsConstructor
public class BookOwnerController {
    private final BookOwnerService bookOwnerService;

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<BookOwnerVO>> findBookOwners() {
        List<BookOwnerVO> bookOwners = bookOwnerService.findAll();
        return ApiResponse.success(bookOwners);
    }

    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{id}")
    public ApiResponse<BookOwnerVO> findBookOwner(@PathVariable("id") long id) {
        BookOwnerVO bookOwner = bookOwnerService.findOne(id);
        return ApiResponse.success(bookOwner);
    }

    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{id}/books")
    public ApiResponse<List<BookVO>> findMyBooks(@PathVariable("id") long id) {
        List<BookVO> myBooks = bookOwnerService.findMyBooks(id);
        return ApiResponse.success(myBooks);
    }

    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{id}/books/sold")
    public ApiResponse<List<BookVO>> findSaleRecordsOfBookOwner(@PathVariable("id") long id) {
        List<BookVO> mySoldBooks = bookOwnerService.findMySoldBooks(id);
        return ApiResponse.success(mySoldBooks);
    }

    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{id}/settlements")
    public ApiResponse<List<SettlementVO>> findMyAllSettlements(@PathVariable("id") long id) {
        List<SettlementVO> allMySettlements = bookOwnerService.findAllMySettlements(id);
        return ApiResponse.success(allMySettlements);
    }

    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{id}/settlements/completed")
    public ApiResponse<List<SettlementVO>> findMySettled(@PathVariable("id") long id) {
        List<SettlementVO> mySettled = bookOwnerService.findMySettled(id);
        return ApiResponse.success(mySettled);
    }

    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/{id}/settlements/pending")
    public ApiResponse<List<SettlementVO>> findMyUnSettled(@PathVariable("id") long id) {
        List<SettlementVO> myUnSettled = bookOwnerService.findMyUnSettled(id);
        return ApiResponse.success(myUnSettled);
    }
}

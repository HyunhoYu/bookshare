package my.api.bookowner;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.exception.ErrorCode;
import my.common.exception.ForbiddenException;
import my.common.response.ApiResponse;
import my.domain.bookowner.service.BookOwnerService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.enums.Role;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
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
    @GetMapping()
    public ApiResponse<List<BookOwnerVO>> findAll() {
        List<BookOwnerVO> bookOwners = bookOwnerService.findAll();

        return ApiResponse.success(bookOwners);
    }

    @RequireRole({Role.ADMIN, Role.BOOK_OWNER})
    @GetMapping("/{id}")
    public ApiResponse<BookOwnerVO> findOne(HttpServletRequest request, @PathVariable long pathVarId) {
        String role = (String) request.getAttribute("userRole");
        Integer id = (Integer) request.getAttribute("userId");

        if (role.equals("BOOK_OWNER") && id != pathVarId) throw new ForbiddenException(ErrorCode.FORBIDDEN);

        BookOwnerVO bookOwner = bookOwnerService.findOne(pathVarId);

        return ApiResponse.success(bookOwner);
    }


}

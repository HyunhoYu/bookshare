package my.api.customer;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.bookowner_profile.BookOwnerProfileVO;
import my.domain.bookowner_profile.service.BookOwnerProfileService;
import my.enums.Role;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/bookstores")
public class CustomerBookstoreController {

    private final BookOwnerProfileService bookOwnerProfileService;

    @RequireRole({Role.CUSTOMER, Role.ADMIN})
    @GetMapping
    public ApiResponse<List<BookOwnerProfileVO>> getAllBookstores() {
        return ApiResponse.success(bookOwnerProfileService.findAllProfiles());
    }
}

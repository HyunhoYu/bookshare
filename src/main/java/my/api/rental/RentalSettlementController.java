package my.api.rental;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.rental.RentalSettlementDetailVO;
import my.domain.rental.RentalSettlementVO;
import my.domain.rental.service.RentalSettlementService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rental-settlements")
public class RentalSettlementController {

    private final RentalSettlementService rentalSettlementService;

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<RentalSettlementDetailVO>> findAll(
            @RequestParam(value = "bookOwnerId", required = false) Long bookOwnerId) {
        List<RentalSettlementDetailVO> result;
        if (bookOwnerId != null) {
            result = rentalSettlementService.findByBookOwnerId(bookOwnerId);
        } else {
            result = rentalSettlementService.findAll();
        }
        return ApiResponse.success(result);
    }

    @RequireRole(value = {Role.ADMIN, Role.BOOK_OWNER}, checkOwnership = true)
    @GetMapping("/book-owner/{id}")
    public ApiResponse<List<RentalSettlementDetailVO>> findByBookOwner(
            @PathVariable("id") Long bookOwnerId) {
        List<RentalSettlementDetailVO> result = rentalSettlementService.findByBookOwnerId(bookOwnerId);
        return ApiResponse.success(result);
    }

    @RequireRole(Role.ADMIN)
    @PostMapping("/{rental-settlement-id}/pay")
    public ApiResponse<RentalSettlementVO> pay(
            @PathVariable("rental-settlement-id") Long id) {
        RentalSettlementVO result = rentalSettlementService.pay(id);
        return ApiResponse.success(result);
    }
}

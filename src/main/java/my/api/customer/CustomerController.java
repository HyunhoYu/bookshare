package my.api.customer;

import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.customer.service.CustomerService;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<UserVO>> findAll() {
        return ApiResponse.success(customerService.findAll());
    }

    @RequireRole(value = {Role.ADMIN, Role.CUSTOMER}, checkOwnership = true)
    @GetMapping("/{id}")
    public ApiResponse<UserVO> findOne(@PathVariable("id") Long id) {
        return ApiResponse.success(customerService.findOne(id));
    }

    @RequireRole(value = {Role.ADMIN, Role.CUSTOMER}, checkOwnership = true)
    @PutMapping("/{id}")
    public ApiResponse<UserVO> update(@PathVariable("id") Long id, @RequestBody UserUpdateDto dto) {
        dto.setId(id);
        return ApiResponse.success(customerService.update(dto));
    }

    @RequireRole(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        customerService.delete(id);
        return ApiResponse.success(null);
    }
}

package my.api.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.common.response.ApiResponse;
import my.domain.user.dto.request.AccountUpdateDto;
import my.domain.user.dto.request.PasswordChangeDto;
import my.domain.user.dto.response.MyAccountDto;
import my.domain.user.service.MyAccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class MyAccountController {

    private final MyAccountService myAccountService;

    @GetMapping("/account")
    public ApiResponse<MyAccountDto> getMyAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(myAccountService.getMyAccount(userId));
    }

    @PutMapping("/account")
    public ApiResponse<MyAccountDto> updateMyAccount(HttpServletRequest request,
                                                      @Valid @RequestBody AccountUpdateDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success("계정 정보 수정 완료", myAccountService.updateMyAccount(userId, dto));
    }

    @PutMapping("/password")
    public ApiResponse<?> changePassword(HttpServletRequest request,
                                          @Valid @RequestBody PasswordChangeDto dto) {
        Long userId = (Long) request.getAttribute("userId");
        myAccountService.changePassword(userId, dto);
        return ApiResponse.success("비밀번호 변경 완료", null);
    }
}

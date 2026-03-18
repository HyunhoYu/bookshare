package my.domain.user.service;

import my.domain.user.dto.request.AccountUpdateDto;
import my.domain.user.dto.request.PasswordChangeDto;
import my.domain.user.dto.response.MyAccountDto;

public interface MyAccountService {
    MyAccountDto getMyAccount(Long userId);
    MyAccountDto updateMyAccount(Long userId, AccountUpdateDto dto);
    void changePassword(Long userId, PasswordChangeDto dto);
}

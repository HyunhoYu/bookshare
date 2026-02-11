package my.domain.customer.service.auth;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.customer.CustomerMapper;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserJoinRequestDto;
import my.domain.user.service.auth.UserAuthService;
import my.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerAuthServiceImpl implements CustomerAuthService {

    private final UserAuthService userAuthService;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserVO signup(UserJoinRequestDto dto) {
        UserVO userVO = UserVO.builder()
                .role(Role.CUSTOMER)
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .residentNumber(dto.getResidentNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        userAuthService.save(userVO);
        int result = customerMapper.insert(userVO.getId());
        if (result != 1) {
            throw new ApplicationException(ErrorCode.CUSTOMER_INSERT_FAIL);
        }
        return userVO;
    }
}

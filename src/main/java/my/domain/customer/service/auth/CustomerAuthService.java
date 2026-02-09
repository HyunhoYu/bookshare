package my.domain.customer.service.auth;

import my.domain.user.UserVO;
import my.domain.user.dto.request.UserJoinRequestDto;

public interface CustomerAuthService {

    UserVO signup(UserJoinRequestDto dto);
}

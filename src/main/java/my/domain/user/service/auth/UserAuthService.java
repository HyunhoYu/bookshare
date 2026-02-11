package my.domain.user.service.auth;

import my.domain.user.UserVO;
import my.domain.user.dto.request.LoginRequestDto;

public interface UserAuthService {

    int save(UserVO userVO);
    UserVO findById(Long id);
    UserVO createUser(UserVO userVO) throws Exception;

    UserVO findByEmail(String email);
    UserVO findByPhone(String phone);
    UserVO findByResidentNumber(String residentNumber);

    UserVO login(LoginRequestDto loginRequestDto);

}

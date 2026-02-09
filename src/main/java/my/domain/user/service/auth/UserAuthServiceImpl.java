package my.domain.user.service.auth;

import lombok.RequiredArgsConstructor;
import my.common.exception.ErrorCode;
import my.common.exception.InCorrectPasswordException;
import my.common.exception.UserInsertFailException;
import my.common.exception.UserNotFoundException;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import my.domain.user.dto.request.LoginRequestDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserVO findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public int save(UserVO userVO) {
        int result = userMapper.insert(userVO);

        if (result != 1) {
            throw new UserInsertFailException(ErrorCode.USER_INSERT_FAIL);
        }

        return result;
    }


    @Override
    public UserVO login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        UserVO user = this.findByEmail(email);

        if (user == null) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        String storedPassword = user.getPassword();
        String inputPassword = loginRequestDto.getPassword();

        if (!passwordEncoder.matches(inputPassword, storedPassword)) {
            throw new InCorrectPasswordException(ErrorCode.INCORRECT_PASSWORD);
        }

        return user;
    }

    @Override
    public UserVO findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    @Transactional
    public UserVO createUser(UserVO userVO) {
        save(userVO);
        return userVO;
    }






}

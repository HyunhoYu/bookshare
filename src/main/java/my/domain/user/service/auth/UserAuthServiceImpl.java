package my.domain.user.service.auth;

import lombok.RequiredArgsConstructor;
import my.common.exception.ErrorCode;
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
        return userMapper.insert(userVO);
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
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
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

        setUserId(userVO);
        int result = save(userVO);


        return userVO;
    }

    private void setUserId(UserVO userVO) {
        userVO.setId(userMapper.selectNextId());
    }






}

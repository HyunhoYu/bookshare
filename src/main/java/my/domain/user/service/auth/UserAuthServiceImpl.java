package my.domain.user.service.auth;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
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
        if (findByEmail(userVO.getEmail()) != null) {
            throw new ApplicationException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (findByPhone(userVO.getPhone()) != null) {
            throw new ApplicationException(ErrorCode.DUPLICATE_PHONE);
        }
        if (findByResidentNumber(userVO.getResidentNumber()) != null) {
            throw new ApplicationException(ErrorCode.DUPLICATE_RESIDENT_NUMBER);
        }

        int result = userMapper.insert(userVO);

        if (result != 1) {
            throw new ApplicationException(ErrorCode.USER_INSERT_FAIL);
        }

        return result;
    }


    @Override
    public UserVO login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        UserVO user = requireNonNull(this.findByEmail(email), ErrorCode.USER_NOT_FOUND);

        String storedPassword = user.getPassword();
        String inputPassword = loginRequestDto.getPassword();

        if (!passwordEncoder.matches(inputPassword, storedPassword)) {
            throw new ApplicationException(ErrorCode.INCORRECT_PASSWORD);
        }

        return user;
    }

    @Override
    public UserVO findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public UserVO findByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public UserVO findByResidentNumber(String residentNumber) {
        return userMapper.selectByResidentNumber(residentNumber);
    }

    @Override
    @Transactional
    public UserVO createUser(UserVO userVO) {
        save(userVO);
        return userVO;
    }



}

package my.domain.user.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.ErrorCode;
import my.common.exception.UserNotFoundException;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserVO findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<UserVO> findAll() {
        return userMapper.selectAll();
    }

    @Override
    public UserVO updateOne(UserUpdateDto dto, Long id) {
        dto.setId(id);

        if (dto.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(encodedPassword);
        }


        int result = userMapper.updateOne(dto);
        if (result == 1) {
            return userMapper.selectById(id);
        }

        throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
    }

    @Override
    public void deleteOne(Long id) {

        UserVO user = userMapper.selectById(id);
        if (user == null) throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        int result = userMapper.softDeleteOne(id);

        if (result != 1) throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);

    }
}

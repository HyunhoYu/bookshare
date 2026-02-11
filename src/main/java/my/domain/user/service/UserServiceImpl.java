package my.domain.user.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public UserVO update(Long id, UserUpdateDto dto) {
        dto.setId(id);

        if (dto.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(encodedPassword);
        }

        int result = userMapper.update(dto);
        if (result == 1) {
            return userMapper.selectById(id);
        }

        throw new ApplicationException(ErrorCode.USER_NOT_FOUND);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireNonNull(userMapper.selectById(id), ErrorCode.USER_NOT_FOUND);
        int result = userMapper.softDeleteOne(id);

        if (result != 1) throw new ApplicationException(ErrorCode.USER_NOT_FOUND);
    }
}

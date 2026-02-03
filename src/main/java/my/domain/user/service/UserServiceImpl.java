package my.domain.user.service;

import lombok.RequiredArgsConstructor;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

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

        int result = userMapper.updateOne(dto);
        if (result == 1) {
            return userMapper.selectById(id);
        }


        return null;
    }


}

package my.domain.user.service;

import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;

import java.util.List;

public interface UserService {

    List<UserVO> findAll();
    UserVO findById(Long id);
    UserVO updateOne(UserUpdateDto dto, Long id);
    void deleteOne(Long id);

}

package my.domain.customer.service;

import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;

import java.util.List;

public interface CustomerService {

    List<UserVO> findAll();
    UserVO findById(Long id);
    UserVO update(UserUpdateDto dto);
    void delete(Long id);
}

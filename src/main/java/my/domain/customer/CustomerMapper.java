package my.domain.customer;

import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerMapper {

    int insert(Long id);
    List<UserVO> selectAll();
    UserVO selectById(Long id);
    int update(UserUpdateDto dto);
    int softDeleteOne(Long id);
}

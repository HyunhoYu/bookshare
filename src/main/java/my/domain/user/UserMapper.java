package my.domain.user;

import my.domain.user.dto.request.UserUpdateDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    int insert(UserVO userVO);
    UserVO selectById(Long id);
    Long selectNextId();
    UserVO selectByEmail(String email);
    List<UserVO> selectAll();
    int updateOne(UserUpdateDto dto);
    int softDeleteOne(Long id);

}

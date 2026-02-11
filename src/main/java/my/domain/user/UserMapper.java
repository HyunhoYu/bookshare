package my.domain.user;

import my.domain.user.dto.request.UserUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    int insert(UserVO userVO);
    UserVO selectById(Long id);
    UserVO selectByEmail(String email);
    List<UserVO> selectAll();
    int update(UserUpdateDto dto);
    int softDeleteOne(Long id);
    UserVO selectByPhone(String phone);
    UserVO selectByResidentNumber(String residentNumber);
    UserVO selectBookOwnerByNameAndPhone(@Param("name") String name, @Param("phone") String phone);
    List<UserVO> selectUsersByPhoneNumberLastFour(@Param("lastFour") String lastFour);

}

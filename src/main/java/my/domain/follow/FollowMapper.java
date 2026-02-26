package my.domain.follow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {
    int insert(FollowVO vo);
    int deleteByCustomerIdAndBookOwnerId(@Param("customerId") Long customerId,
                                          @Param("bookOwnerId") Long bookOwnerId);
    FollowVO selectByCustomerIdAndBookOwnerId(@Param("customerId") Long customerId,
                                               @Param("bookOwnerId") Long bookOwnerId);
    List<FollowVO> selectByCustomerId(Long customerId);
    int countByBookOwnerId(Long bookOwnerId);
}

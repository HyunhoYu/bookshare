package my.domain.wishlist;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishlistMapper {
    int insert(@Param("customerId") Long customerId, @Param("bookId") Long bookId);
    int delete(@Param("customerId") Long customerId, @Param("bookId") Long bookId);
    List<WishlistVO> selectByCustomerId(Long customerId);
    int checkExists(@Param("customerId") Long customerId, @Param("bookId") Long bookId);
}

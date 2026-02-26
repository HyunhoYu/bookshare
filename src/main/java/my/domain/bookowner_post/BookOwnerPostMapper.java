package my.domain.bookowner_post;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookOwnerPostMapper {
    int insert(BookOwnerPostVO vo);
    int update(BookOwnerPostVO vo);
    int softDelete(Long id);
    BookOwnerPostVO selectById(Long id);
    List<BookOwnerPostVO> selectByBookOwnerId(Long bookOwnerId);
    List<BookOwnerPostVO> selectAll();
    List<BookOwnerPostVO> selectFeedByCustomerId(Long customerId);
}

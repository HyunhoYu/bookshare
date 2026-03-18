package my.domain.post_like;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper {
    int insert(PostLikeVO vo);
    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    PostLikeVO selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    int countByPostId(Long postId);
}

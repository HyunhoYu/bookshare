package my.domain.post_comment;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostCommentMapper {
    int insert(PostCommentVO vo);
    int update(PostCommentVO vo);
    int softDelete(Long id);
    PostCommentVO selectById(Long id);
    List<PostCommentVO> selectTopLevelByPostId(Long postId);
    List<PostCommentVO> selectRepliesByParentId(Long parentId);
}

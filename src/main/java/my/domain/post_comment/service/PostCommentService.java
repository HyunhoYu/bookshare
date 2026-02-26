package my.domain.post_comment.service;

import my.domain.post_comment.PostCommentVO;
import my.domain.post_comment.dto.PostCommentCreateDto;
import my.domain.post_comment.dto.PostCommentUpdateDto;

import java.util.List;

public interface PostCommentService {
    PostCommentVO create(Long postId, Long userId, PostCommentCreateDto dto);
    PostCommentVO update(Long commentId, Long userId, PostCommentUpdateDto dto);
    void delete(Long commentId, Long userId);
    List<PostCommentVO> findByPostId(Long postId);
}

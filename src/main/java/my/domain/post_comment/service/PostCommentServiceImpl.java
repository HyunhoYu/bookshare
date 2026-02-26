package my.domain.post_comment.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookowner_post.BookOwnerPostMapper;
import my.domain.post_comment.PostCommentMapper;
import my.domain.post_comment.PostCommentVO;
import my.domain.post_comment.dto.PostCommentCreateDto;
import my.domain.post_comment.dto.PostCommentUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentMapper commentMapper;
    private final BookOwnerPostMapper postMapper;

    @Override
    @Transactional
    public PostCommentVO create(Long postId, Long userId, PostCommentCreateDto dto) {
        requireNonNull(postMapper.selectById(postId), ErrorCode.POST_NOT_FOUND);

        if (dto.getParentId() != null) {
            PostCommentVO parent = requireNonNull(
                    commentMapper.selectById(dto.getParentId()), ErrorCode.COMMENT_NOT_FOUND);
            if (parent.getParentId() != null) {
                throw new ApplicationException(ErrorCode.COMMENT_NOT_REPLY_TARGET);
            }
        }

        PostCommentVO vo = new PostCommentVO();
        vo.setPostId(postId);
        vo.setUserId(userId);
        vo.setParentId(dto.getParentId());
        vo.setContent(dto.getContent());

        int result = commentMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.COMMENT_INSERT_FAIL);
        }

        return commentMapper.selectById(vo.getId());
    }

    @Override
    @Transactional
    public PostCommentVO update(Long commentId, Long userId, PostCommentUpdateDto dto) {
        PostCommentVO existing = requireNonNull(commentMapper.selectById(commentId), ErrorCode.COMMENT_NOT_FOUND);

        if (!existing.getUserId().equals(userId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        existing.setContent(dto.getContent());
        commentMapper.update(existing);

        return commentMapper.selectById(commentId);
    }

    @Override
    @Transactional
    public void delete(Long commentId, Long userId) {
        PostCommentVO existing = requireNonNull(commentMapper.selectById(commentId), ErrorCode.COMMENT_NOT_FOUND);

        if (!existing.getUserId().equals(userId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        commentMapper.softDelete(commentId);
    }

    @Override
    public List<PostCommentVO> findByPostId(Long postId) {
        List<PostCommentVO> topLevel = commentMapper.selectTopLevelByPostId(postId);
        for (PostCommentVO comment : topLevel) {
            comment.setReplies(commentMapper.selectRepliesByParentId(comment.getId()));
        }
        return topLevel;
    }
}

package my.domain.post_like.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookowner_post.BookOwnerPostMapper;
import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.notification.service.NotificationService;
import my.domain.post_like.PostLikeMapper;
import my.domain.post_like.PostLikeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeMapper postLikeMapper;
    private final BookOwnerPostMapper postMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void like(Long postId, Long userId) {
        BookOwnerPostVO post = requireNonNull(postMapper.selectById(postId), ErrorCode.POST_NOT_FOUND);

        if (postLikeMapper.selectByPostIdAndUserId(postId, userId) != null) {
            throw new ApplicationException(ErrorCode.ALREADY_LIKED);
        }

        PostLikeVO vo = new PostLikeVO();
        vo.setPostId(postId);
        vo.setUserId(userId);

        int result = postLikeMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.LIKE_INSERT_FAIL);
        }

        // BookOwner notify (skip self-like)
        if (!post.getBookOwnerId().equals(userId)) {
            int likeCount = postLikeMapper.countByPostId(postId);
            notificationService.create(
                    post.getBookOwnerId(),
                    "NEW_LIKE",
                    "게시글 \"" + post.getTitle() + "\"에 좋아요가 눌렸습니다. (총 " + likeCount + "개)",
                    postId
            );
        }
    }

    @Override
    @Transactional
    public void unlike(Long postId, Long userId) {
        requireNonNull(postMapper.selectById(postId), ErrorCode.POST_NOT_FOUND);

        if (postLikeMapper.selectByPostIdAndUserId(postId, userId) == null) {
            throw new ApplicationException(ErrorCode.NOT_LIKED);
        }

        postLikeMapper.deleteByPostIdAndUserId(postId, userId);
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        return postLikeMapper.selectByPostIdAndUserId(postId, userId) != null;
    }

    @Override
    public int getLikeCount(Long postId) {
        return postLikeMapper.countByPostId(postId);
    }
}

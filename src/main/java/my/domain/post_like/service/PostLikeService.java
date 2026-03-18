package my.domain.post_like.service;

public interface PostLikeService {
    void like(Long postId, Long userId);
    void unlike(Long postId, Long userId);
    boolean isLiked(Long postId, Long userId);
    int getLikeCount(Long postId);
}

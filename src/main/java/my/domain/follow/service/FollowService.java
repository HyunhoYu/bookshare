package my.domain.follow.service;

import my.domain.follow.FollowVO;

import java.util.List;

public interface FollowService {
    FollowVO follow(Long customerId, Long bookOwnerId);
    void unfollow(Long customerId, Long bookOwnerId);
    List<FollowVO> getFollowList(Long customerId);
    boolean isFollowing(Long customerId, Long bookOwnerId);
    int getFollowerCount(Long bookOwnerId);
}

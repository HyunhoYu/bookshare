package my.domain.follow.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.customer.CustomerMapper;
import my.domain.follow.FollowMapper;
import my.domain.follow.FollowVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final CustomerMapper customerMapper;
    private final BookOwnerMapper bookOwnerMapper;

    @Override
    @Transactional
    public FollowVO follow(Long customerId, Long bookOwnerId) {
        requireNonNull(customerMapper.selectById(customerId), ErrorCode.CUSTOMER_NOT_FOUND);
        requireNonNull(bookOwnerMapper.selectById(bookOwnerId), ErrorCode.BOOK_OWNER_NOT_FOUND);

        if (followMapper.selectByCustomerIdAndBookOwnerId(customerId, bookOwnerId) != null) {
            throw new ApplicationException(ErrorCode.ALREADY_FOLLOWING);
        }

        FollowVO vo = new FollowVO();
        vo.setCustomerId(customerId);
        vo.setBookOwnerId(bookOwnerId);

        int result = followMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.FOLLOW_INSERT_FAIL);
        }

        return vo;
    }

    @Override
    @Transactional
    public void unfollow(Long customerId, Long bookOwnerId) {
        if (followMapper.selectByCustomerIdAndBookOwnerId(customerId, bookOwnerId) == null) {
            throw new ApplicationException(ErrorCode.NOT_FOLLOWING);
        }

        followMapper.deleteByCustomerIdAndBookOwnerId(customerId, bookOwnerId);
    }

    @Override
    public List<FollowVO> getFollowList(Long customerId) {
        return followMapper.selectByCustomerId(customerId);
    }

    @Override
    public boolean isFollowing(Long customerId, Long bookOwnerId) {
        return followMapper.selectByCustomerIdAndBookOwnerId(customerId, bookOwnerId) != null;
    }

    @Override
    public int getFollowerCount(Long bookOwnerId) {
        return followMapper.countByBookOwnerId(bookOwnerId);
    }
}

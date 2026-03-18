package my.domain.wishlist.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.wishlist.WishlistMapper;
import my.domain.wishlist.WishlistVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional
    public void addWishlist(Long customerId, Long bookId) {
        if (wishlistMapper.checkExists(customerId, bookId) > 0) {
            throw new ApplicationException(ErrorCode.WISHLIST_ALREADY_EXISTS);
        }
        int result = wishlistMapper.insert(customerId, bookId);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.WISHLIST_INSERT_FAIL);
        }
    }

    @Override
    @Transactional
    public void removeWishlist(Long customerId, Long bookId) {
        int result = wishlistMapper.delete(customerId, bookId);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.WISHLIST_NOT_FOUND);
        }
    }

    @Override
    public List<WishlistVO> getMyWishlist(Long customerId) {
        return wishlistMapper.selectByCustomerId(customerId);
    }

    @Override
    public boolean checkWishlist(Long customerId, Long bookId) {
        return wishlistMapper.checkExists(customerId, bookId) > 0;
    }
}

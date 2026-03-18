package my.domain.wishlist.service;

import my.domain.wishlist.WishlistVO;

import java.util.List;

public interface WishlistService {
    void addWishlist(Long customerId, Long bookId);
    void removeWishlist(Long customerId, Long bookId);
    List<WishlistVO> getMyWishlist(Long customerId);
    boolean checkWishlist(Long customerId, Long bookId);
}

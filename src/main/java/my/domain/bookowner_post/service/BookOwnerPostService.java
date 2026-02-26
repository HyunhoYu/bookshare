package my.domain.bookowner_post.service;

import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.bookowner_post.dto.BookOwnerPostCreateDto;
import my.domain.bookowner_post.dto.BookOwnerPostUpdateDto;

import java.util.List;

public interface BookOwnerPostService {
    BookOwnerPostVO create(Long bookOwnerId, BookOwnerPostCreateDto dto);
    BookOwnerPostVO update(Long postId, Long bookOwnerId, BookOwnerPostUpdateDto dto);
    void delete(Long postId, Long bookOwnerId);
    BookOwnerPostVO findById(Long postId);
    List<BookOwnerPostVO> findByBookOwnerId(Long bookOwnerId);
    List<BookOwnerPostVO> findAll();
    List<BookOwnerPostVO> findFeed(Long customerId);
}

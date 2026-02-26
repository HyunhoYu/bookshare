package my.domain.bookowner_post.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner_post.BookOwnerPostMapper;
import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.bookowner_post.dto.BookOwnerPostCreateDto;
import my.domain.bookowner_post.dto.BookOwnerPostUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookOwnerPostServiceImpl implements BookOwnerPostService {

    private final BookOwnerPostMapper postMapper;
    private final BookOwnerMapper bookOwnerMapper;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookOwnerPostVO create(Long bookOwnerId, BookOwnerPostCreateDto dto) {
        requireNonNull(bookOwnerMapper.selectById(bookOwnerId), ErrorCode.BOOK_OWNER_NOT_FOUND);

        if (dto.getBookId() != null) {
            BookVO book = requireNonNull(bookMapper.selectById(dto.getBookId()), ErrorCode.BOOK_NOT_FOUND);
            if (!book.getBookOwnerId().equals(bookOwnerId)) {
                throw new ApplicationException(ErrorCode.POST_BOOK_OWNER_MISMATCH);
            }
        }

        BookOwnerPostVO vo = new BookOwnerPostVO();
        vo.setBookOwnerId(bookOwnerId);
        vo.setBookId(dto.getBookId());
        vo.setTitle(dto.getTitle());
        vo.setContent(dto.getContent());

        int result = postMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.POST_INSERT_FAIL);
        }

        return postMapper.selectById(vo.getId());
    }

    @Override
    @Transactional
    public BookOwnerPostVO update(Long postId, Long bookOwnerId, BookOwnerPostUpdateDto dto) {
        BookOwnerPostVO existing = requireNonNull(postMapper.selectById(postId), ErrorCode.POST_NOT_FOUND);

        if (!existing.getBookOwnerId().equals(bookOwnerId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        if (dto.getBookId() != null) {
            BookVO book = requireNonNull(bookMapper.selectById(dto.getBookId()), ErrorCode.BOOK_NOT_FOUND);
            if (!book.getBookOwnerId().equals(bookOwnerId)) {
                throw new ApplicationException(ErrorCode.POST_BOOK_OWNER_MISMATCH);
            }
        }

        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setBookId(dto.getBookId());

        postMapper.update(existing);
        return postMapper.selectById(postId);
    }

    @Override
    @Transactional
    public void delete(Long postId, Long bookOwnerId) {
        BookOwnerPostVO existing = requireNonNull(postMapper.selectById(postId), ErrorCode.POST_NOT_FOUND);

        if (!existing.getBookOwnerId().equals(bookOwnerId)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN);
        }

        postMapper.softDelete(postId);
    }

    @Override
    public BookOwnerPostVO findById(Long postId) {
        return postMapper.selectById(postId);
    }

    @Override
    public List<BookOwnerPostVO> findByBookOwnerId(Long bookOwnerId) {
        return postMapper.selectByBookOwnerId(bookOwnerId);
    }

    @Override
    public List<BookOwnerPostVO> findAll() {
        return postMapper.selectAll();
    }

    @Override
    public List<BookOwnerPostVO> findFeed(Long customerId) {
        return postMapper.selectFeedByCustomerId(customerId);
    }
}

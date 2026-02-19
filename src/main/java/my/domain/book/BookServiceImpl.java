package my.domain.book;

import java.util.List;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.enums.BookState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    @Override
    public List<BookVO> findAll() {
        return bookMapper.selectAll();
    }

    @Override
    public List<BookVO> findBooksByBookOwnerId(Long id) {
        return bookMapper.selectBooksByBookOwnerId(id);
    }

    @Override
    public List<BookVO> findSoldBookOfBookOwner(Long bookOwnerId) {
        return bookMapper.selectSoldBookByBookOwnerId(bookOwnerId);
    }

    @Override
    @Transactional
    public List<Long> retrieveBooks(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_RETRIEVE_REQUEST);
        }

        int count = bookMapper.countByIdsAndState(bookIds, BookState.SHOULD_BE_RETRIEVED.name());
        if (count != bookIds.size()) {
            throw new ApplicationException(ErrorCode.BOOK_NOT_RETRIEVABLE);
        }

        int result = bookMapper.softDeleteBooks(bookIds);
        if (result != bookIds.size()) {
            throw new ApplicationException(ErrorCode.RETRIEVE_FAIL);
        }

        return bookIds;
    }

    @Override
    public List<BookWithBookCaseVO> findAllWithBookCase(String state) {
        return bookMapper.selectAllWithBookCase(state);
    }

    @Override
    public List<BookVO> findByBookCaseId(Long bookCaseId) {
        return bookMapper.selectByBookCaseId(bookCaseId);
    }

}

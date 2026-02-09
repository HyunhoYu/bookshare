package my.domain.bookcase.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.BookCaseAlreadyOccupiedException;
import my.common.exception.BookCaseNotFoundException;
import my.common.exception.BookCaseTypeNotFoundException;
import my.common.exception.ErrorCode;
import my.domain.bookcase.BookCaseMapper;
import my.domain.bookcase.BookCaseOccupiedRecordMapper;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcasetype.BookCaseTypeMapper;
import my.domain.bookcasetype.BookCaseTypeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCaseServiceImpl implements BookCaseService {

    private final BookCaseMapper bookCaseMapper;
    private final BookCaseTypeMapper bookCaseTypeMapper;
    private final BookCaseOccupiedRecordMapper occupiedRecordMapper;

    @Override
    public long addBookCase(BookCaseVO bookCaseVO) {
        // 책장 타입이 실제로 존재하는지 검증
        BookCaseTypeVO bookCaseType = bookCaseTypeMapper.selectById(bookCaseVO.getBookCaseTypeId());
        if (bookCaseType == null) {
            throw new BookCaseTypeNotFoundException(ErrorCode.BOOK_CASE_TYPE_NOT_FOUND);
        }

        int result = bookCaseMapper.insertBookCase(bookCaseVO);
        if (result != 1) {
            throw new RuntimeException("책장 저장 실패");
        }
        return bookCaseVO.getId();
    }

    @Override
    public BookCaseVO findById(long id) {
        return bookCaseMapper.selectById(id);
    }

    @Override
    public List<BookCaseVO> findAll() {
        return bookCaseMapper.selectAll();
    }

    @Override
    public List<BookCaseVO> findUsableBookCases() {
        return bookCaseMapper.selectUsableBookCases();
    }


    @Override
    public boolean isOccupied(long bookCaseId) {
        return occupiedRecordMapper.countCurrentOccupied(bookCaseId) > 0;
    }


    @Override
    @Transactional
    public BookCaseOccupiedRecordVO occupy(long bookOwnerId, long bookCaseId) {
        BookCaseVO bookCase = bookCaseMapper.selectById(bookCaseId);
        if (bookCase == null) {
            throw new BookCaseNotFoundException(ErrorCode.BOOK_CASE_NOT_FOUND);
        }

        if (isOccupied(bookCaseId)) {
            throw new BookCaseAlreadyOccupiedException(ErrorCode.BOOK_CASE_ALREADY_OCCUPIED);
        }

        BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
        record.setBookOwnerId(bookOwnerId);
        record.setBookCaseId(bookCaseId);

        occupiedRecordMapper.insert(record);
        return occupiedRecordMapper.selectById(record.getId());
    }
}

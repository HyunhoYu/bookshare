package my.domain.bookcasetype.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.BookCaseTypeInsertFailException;
import my.common.exception.ErrorCode;
import my.domain.bookcasetype.BookCaseTypeMapper;
import my.domain.bookcasetype.BookCaseTypeVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCaseTypeServiceImpl implements BookCaseTypeService {

    private final BookCaseTypeMapper bookCaseTypeMapper;

    @Override
    public long addBookCaseType(BookCaseTypeVO bookCaseTypeVO) {
        int result = bookCaseTypeMapper.insertBookCaseType(bookCaseTypeVO);
        if (result != 1) throw new BookCaseTypeInsertFailException(ErrorCode.BOOK_CASE_TYPE_INSERT_FAIL);
        return bookCaseTypeVO.getId();
    }

    @Override
    public BookCaseTypeVO findById(long id) {
        return bookCaseTypeMapper.selectById(id);
    }

    @Override
    public List<BookCaseTypeVO> findAll() {
        return bookCaseTypeMapper.selectAll();
    }
}

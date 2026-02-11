package my.domain.bookcasetype.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.BookCaseTypeMapper;
import my.domain.bookcasetype.BookCaseTypeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCaseTypeServiceImpl implements BookCaseTypeService {

    private final BookCaseTypeMapper bookCaseTypeMapper;

    @Override
    @Transactional
    public long create(BookCaseTypeCreateDto bookCaseTypeCreateDto) {
        if (bookCaseTypeMapper.selectByCode(bookCaseTypeCreateDto.getCode()) != null) {
            throw new ApplicationException(ErrorCode.DUPLICATE_BOOK_CASE_TYPE_CODE);
        }

        BookCaseTypeVO bookCaseTypeVO = new BookCaseTypeVO();

        bookCaseTypeVO.setCode(bookCaseTypeCreateDto.getCode());
        bookCaseTypeVO.setMonthlyPrice(bookCaseTypeCreateDto.getMonthlyPrice());

        int result = bookCaseTypeMapper.insert(bookCaseTypeVO);
        if (result != 1) throw new ApplicationException(ErrorCode.BOOK_CASE_TYPE_INSERT_FAIL);
        return bookCaseTypeVO.getId();
    }

    @Override
    public BookCaseTypeVO findById(Long id) {
        return bookCaseTypeMapper.selectById(id);
    }

    @Override
    public List<BookCaseTypeVO> findAll() {
        return bookCaseTypeMapper.selectAll();
    }
}

package my.domain.bookcase.service;

import my.domain.book.BookVO;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.BookRegisterDto;

import java.util.List;
import java.util.Map;

public interface BookCaseService {

    long addBookCase(BookCaseVO bookCaseVO);
    BookCaseVO findById(long id);
    List<BookCaseVO> findAll();
    List<BookCaseVO> findUsableBookCases();
    boolean isOccupied(long bookCaseId);
    BookCaseOccupiedRecordVO occupy(long bookOwnerId, long bookCaseId);
    List<BookVO> registerBooks(long bookcaseId, List<BookRegisterDto> bookRegisterDtos);
    List<Long> selectMyOccupyingBookCasesByBookOwnerId(Long bookOwnerId);
    List<Long> unOccupyProcess(List<Long> bookCaseIds);
}

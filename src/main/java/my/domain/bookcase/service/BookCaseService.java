package my.domain.bookcase.service;

import my.domain.book.BookVO;
import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.BookCaseVO;
import my.domain.bookcase.BookCaseWithOccupationVO;
import my.domain.bookcase.BookRegisterDto;

import java.time.LocalDate;
import java.util.List;

public interface BookCaseService {

    long create(BookCaseCreateDto dto);
    BookCaseVO findById(Long id);
    List<BookCaseVO> findAll();
    List<BookCaseVO> findUsable();
    boolean isOccupied(Long bookCaseId);
    List<BookCaseOccupiedRecordVO> occupy(Long bookOwnerId, List<Long> bookCaseIds, LocalDate expirationDate, int depositAmount);
    List<BookVO> registerBooks(Long bookcaseId, List<BookRegisterDto> bookRegisterDtos);
    List<Long> findOccupyingBookCaseIds(Long bookOwnerId);
    List<Long> unOccupyProcess(List<Long> bookCaseIds);
    List<BookCaseWithOccupationVO> findAllWithOccupation();

}

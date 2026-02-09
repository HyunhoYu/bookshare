package my.domain.bookcasetype.service;

import my.domain.bookcasetype.BookCaseTypeVO;

import java.util.List;

public interface BookCaseTypeService {

    long addBookCaseType(BookCaseTypeVO bookCaseTypeVO);
    BookCaseTypeVO findById(long id);
    List<BookCaseTypeVO> findAll();

}

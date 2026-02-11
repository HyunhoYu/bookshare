package my.domain.bookcasetype.service;

import my.domain.bookcasetype.BookCaseTypeCreateDto;
import my.domain.bookcasetype.BookCaseTypeVO;

import java.util.List;

public interface BookCaseTypeService {

    long create(BookCaseTypeCreateDto bookCaseTypeCreateDto);
    BookCaseTypeVO findById(Long id);
    List<BookCaseTypeVO> findAll();

}

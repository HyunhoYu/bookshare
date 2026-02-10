package my.domain.book;

import java.util.List;

public interface BookService {

    List<BookVO> findAll();
    List<BookVO> findBooksByBookOwnerId(Long id);
    List<BookVO> findSoldBookOfBookOwner(Long id);
    List<Long> retrieveBooks(List<Long> bookIds);

}

package my.domain.bookowner.service;

import my.domain.bookowner.vo.BookOwnerVO;

import java.util.List;

public interface BookOwnerService {

    List<BookOwnerVO> findAll();

    BookOwnerVO findOne(Long id);
}

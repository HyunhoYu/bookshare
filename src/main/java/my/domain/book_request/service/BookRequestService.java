package my.domain.book_request.service;

import my.domain.book_request.BookRequestVO;
import my.domain.book_request.dto.BookRequestCreateDto;
import my.domain.book_request.dto.BookRequestStatusUpdateDto;

import java.util.List;

public interface BookRequestService {
    BookRequestVO create(Long customerId, BookRequestCreateDto dto);
    List<BookRequestVO> findByCustomerId(Long customerId);
    List<BookRequestVO> findAll();
    BookRequestVO updateStatus(Long requestId, BookRequestStatusUpdateDto dto);
    BookRequestVO findById(Long id);
}

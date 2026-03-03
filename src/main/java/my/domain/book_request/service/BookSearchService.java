package my.domain.book_request.service;

import my.domain.book_request.dto.BookSearchResultDto;

import java.util.List;

public interface BookSearchService {
    List<BookSearchResultDto> search(String query);
}

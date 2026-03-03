package my.domain.book_request.service;

import my.domain.book_request.dto.BookSearchResultDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
public class MockBookSearchService implements BookSearchService {

    @Override
    public List<BookSearchResultDto> search(String query) {
        return List.of(
                new BookSearchResultDto(
                        "9788936433598",
                        "채식주의자",
                        "한강",
                        "창비",
                        "",
                        "한강 장편소설"
                )
        );
    }
}

package my.domain.book_request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResultDto {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String thumbnail;
    private String description;
}

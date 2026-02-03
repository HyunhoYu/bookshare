package my.domain.bookcase;

import lombok.Getter;
import lombok.Setter;
import my.enums.BookCaseType;

@Getter
@Setter
public class BookCaseVO {
    private Long id;
    private Long currentBookOwnerId;
    private String locationCode;
    private BookCaseType type;
}

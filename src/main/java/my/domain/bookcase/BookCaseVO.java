package my.domain.bookcase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseVO {
    private Long id;
    private String locationCode;
    private Long bookCaseTypeId;
}

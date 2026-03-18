package my.domain.book;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CustomerBookDetailVO {
    // Book
    private Long id;
    private String bookName;
    private String publisherHouse;
    private int price;
    private Date enteredAt;

    // BookOwner
    private Long bookOwnerId;
    private String bookOwnerName;
    private String bookOwnerNickname;

    // Genre (COMMON_CODE: BOOK_TYPE)
    private String genreCode;
    private String genreName;

    // Location (COMMON_CODE: LOCATION via BOOK_CASE)
    private String locationCode;
    private String locationName;

    // BookCase
    private Long bookCaseId;
    private String bookCaseTypeCode;

    // ISBN/Author/Cover
    private String isbn;
    private String author;
    private String thumbnailUrl;
}

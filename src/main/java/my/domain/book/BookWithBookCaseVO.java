package my.domain.book;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class BookWithBookCaseVO {
    // Book 필드
    private Long id;
    private Long bookOwnerId;
    private Long bookCaseId;
    private String bookName;
    private String publisherHouse;
    private int price;
    private Date enteredAt;
    private String bookGroupCodeId;
    private String bookCommonCodeId;
    private String state;

    // ISBN/Author/Cover
    private String isbn;
    private String author;
    private String thumbnailUrl;

    // BookCase 필드
    private String bookCaseGroupCodeId;
    private String bookCaseCommonCodeId;
    private Long bookCaseTypeId;
}

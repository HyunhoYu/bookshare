package my.domain.book;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import my.enums.BookState;

@Getter
@Setter
public class BookVO {
    private Long id;
    private Long bookOwnerId;
    private String bookName;
    private String publisherHouse;
    private int price;
    private Date enteredAt;
    private String isSaled;
    private String typeCode;
    private String state;

    public BookVO() {
        setState(BookState.NORMAL.name());
    }
}

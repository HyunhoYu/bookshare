package my.domain.bookcase;

import lombok.Getter;
import lombok.Setter;
import my.enums.BookState;

@Getter
@Setter
public class BookRegisterDto {

    private String userName;
    private String userPhone;

    private String bookName;
    private String publisherHouse;
    private int price;
    private BookState bookState;
    private String bookType; // 이걸로  common_code에서 코드 조회

}

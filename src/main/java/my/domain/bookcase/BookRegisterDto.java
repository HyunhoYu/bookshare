package my.domain.bookcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import my.enums.BookState;

@Getter
@Setter
public class BookRegisterDto {

    @NotBlank(message = "소유주 이름은 필수입니다")
    private String userName;

    @NotBlank(message = "소유주 전화번호는 필수입니다")
    private String userPhone;

    @NotBlank(message = "책 이름은 필수입니다")
    private String bookName;

    private String publisherHouse;

    @Positive(message = "가격은 0보다 커야 합니다")
    private int price;

    private BookState bookState;

    @NotBlank(message = "책 분류는 필수입니다")
    private String bookType;
}

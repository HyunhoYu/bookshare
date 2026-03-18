package my.domain.bookcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;
import my.enums.BookState;

@Getter
@Setter
public class BookRegisterDto {

    @NotBlank(message = "소유주 이름은 필수입니다")
    @Size(max = 100, message = "소유주 이름은 100자 이내여야 합니다")
    private String userName;

    @NotBlank(message = "소유주 전화번호는 필수입니다")
    @Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    private String userPhone;

    @NotBlank(message = "책 이름은 필수입니다")
    @Size(max = 200, message = "책 이름은 200자 이내여야 합니다")
    private String bookName;

    @Size(max = 200, message = "출판사는 200자 이내여야 합니다")
    private String publisherHouse;

    @Positive(message = "가격은 0보다 커야 합니다")
    @Max(value = 10000000, message = "가격은 1,000만원 이하여야 합니다")
    private int price;

    private BookState bookState;

    @NotBlank(message = "책 분류 코드는 필수입니다")
    @Size(max = 10, message = "책 분류 코드는 10자 이내여야 합니다")
    private String bookTypeCode;

    @Size(max = 13, message = "ISBN은 13자 이내여야 합니다")
    private String isbn;

    @Size(max = 200, message = "저자는 200자 이내여야 합니다")
    private String author;

    @Size(max = 500, message = "표지 URL은 500자 이내여야 합니다")
    private String thumbnailUrl;
}

package my.domain.book_request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequestCreateDto {
    @NotBlank(message = "ISBN은 필수입니다")
    @Pattern(regexp = "^\\d{10}(\\d{3})?$", message = "ISBN은 10자리 또는 13자리 숫자여야 합니다")
    private String isbn;

    @NotBlank(message = "책 제목은 필수입니다")
    @Size(min = 1, max = 200, message = "책 제목은 1~200자여야 합니다")
    private String bookTitle;

    @Size(max = 200, message = "저자는 200자 이내여야 합니다")
    private String author;

    @Size(max = 200, message = "출판사는 200자 이내여야 합니다")
    private String publisher;

    @Size(max = 500, message = "썸네일 URL은 500자 이내여야 합니다")
    @Pattern(regexp = "^(https?://.*)?$", message = "썸네일 URL 형식이 올바르지 않습니다")
    private String thumbnailUrl;
}

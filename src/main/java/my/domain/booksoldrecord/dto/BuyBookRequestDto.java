package my.domain.booksoldrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyBookRequestDto {

    @NotNull(message = "책 ID는 필수입니다")
    @Positive(message = "책 ID는 양수여야 합니다")
    private Long bookId;

    private Long customerId;

    @NotBlank(message = "구매 유형 코드는 필수입니다")
    @Pattern(regexp = "^0[1-5]$", message = "구매 유형 코드는 01~05만 가능합니다")
    private String buyTypeCommonCode;
}

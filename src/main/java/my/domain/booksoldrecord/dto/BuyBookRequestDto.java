package my.domain.booksoldrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyBookRequestDto {

    @NotNull(message = "책 ID는 필수입니다")
    private Long bookId;

    private Long customerId;

    @NotBlank(message = "구매 유형 코드는 필수입니다")
    private String buyTypeCommonCode;
}

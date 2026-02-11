package my.domain.bookcasetype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseTypeCreateDto {
    @NotBlank(message = "코드는 필수입니다")
    private String code;

    @NotNull(message = "월 임대료는 필수입니다")
    @Positive(message = "월 임대료는 0보다 커야 합니다")
    private Integer monthlyPrice;
}

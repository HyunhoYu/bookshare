package my.domain.bookcasetype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseTypeCreateDto {
    @NotBlank(message = "코드는 필수입니다")
    @Size(max = 50, message = "코드는 50자 이내여야 합니다")
    @Pattern(regexp = "^[A-Z_]+$", message = "코드는 영문 대문자와 언더스코어만 가능합니다")
    private String code;

    @NotNull(message = "월 임대료는 필수입니다")
    @Positive(message = "월 임대료는 0보다 커야 합니다")
    @Max(value = 100000000, message = "월 임대료는 1억원 이하여야 합니다")
    private Integer monthlyPrice;
}

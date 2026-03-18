package my.domain.bookcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseCreateDto {
    @NotBlank(message = "위치 코드는 필수입니다")
    @Pattern(regexp = "^0[1-9]$", message = "위치 코드 형식이 올바르지 않습니다 (예: 01, 02, 03)")
    private String locationCode;

    @NotNull(message = "책장 타입 ID는 필수입니다")
    @Positive(message = "책장 타입 ID는 양수여야 합니다")
    private Long bookCaseTypeId;
}

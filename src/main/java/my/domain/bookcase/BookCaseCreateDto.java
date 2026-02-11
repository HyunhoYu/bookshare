package my.domain.bookcase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseCreateDto {
    @NotBlank(message = "위치명은 필수입니다")
    private String locationName;

    @NotNull(message = "책장 타입 ID는 필수입니다")
    private Long bookCaseTypeId;
}

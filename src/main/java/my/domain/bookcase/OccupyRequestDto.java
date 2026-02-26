package my.domain.bookcase;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OccupyRequestDto {
    @NotNull(message = "책 소유주 ID는 필수입니다")
    private Long bookOwnerId;

    @NotEmpty(message = "책장 목록은 비어있을 수 없습니다")
    private List<Long> bookCaseIds;

    @NotNull
    @Future
    private LocalDate expirationDate;

    @NotNull(message = "보증금은 필수입니다")
    @Positive(message = "보증금은 양수여야 합니다")
    private Integer depositAmount;
}

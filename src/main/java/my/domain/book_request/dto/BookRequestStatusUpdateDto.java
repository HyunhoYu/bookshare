package my.domain.book_request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequestStatusUpdateDto {

    @NotBlank(message = "상태는 필수입니다")
    @Pattern(regexp = "APPROVED|REJECTED", message = "상태는 APPROVED 또는 REJECTED만 가능합니다")
    private String status;

    @Size(max = 500, message = "관리자 메모는 500자 이내여야 합니다")
    private String adminComment;
}

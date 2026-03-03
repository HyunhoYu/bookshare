package my.domain.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaCreateDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 4000, message = "내용은 4000자 이내여야 합니다")
    private String content;
}

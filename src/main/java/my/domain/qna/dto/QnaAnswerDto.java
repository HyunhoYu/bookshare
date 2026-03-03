package my.domain.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaAnswerDto {

    @NotBlank(message = "답변 내용은 필수입니다")
    @Size(max = 4000, message = "답변은 4000자 이내여야 합니다")
    private String answer;
}

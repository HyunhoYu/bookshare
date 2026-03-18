package my.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateDto {
    @Min(value = 1, message = "평점은 1~5 사이여야 합니다")
    @Max(value = 5, message = "평점은 1~5 사이여야 합니다")
    private int rating;

    @Size(max = 1000, message = "리뷰는 1000자 이내여야 합니다")
    private String content;
}

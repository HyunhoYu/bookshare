package my.domain.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewSummaryDto {
    private double averageRating;
    private int reviewCount;
}

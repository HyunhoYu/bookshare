package my.domain.settlement_ratio.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import my.annotation.ValidSettlementRatio;

@Getter
@Setter
@ValidSettlementRatio
public class SettlementRatioRequestDto {
    @DecimalMin(value = "0.0", message = "소유주 비율은 0 이상이어야 합니다")
    @DecimalMax(value = "1.0", message = "소유주 비율은 1 이하여야 합니다")
    private double ownerRatio;

    @DecimalMin(value = "0.0", message = "서점 비율은 0 이상이어야 합니다")
    @DecimalMax(value = "1.0", message = "서점 비율은 1 이하여야 합니다")
    private double storeRatio;
}

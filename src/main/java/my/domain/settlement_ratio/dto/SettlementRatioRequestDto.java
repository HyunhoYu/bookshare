package my.domain.settlement_ratio.dto;

import lombok.Getter;
import lombok.Setter;
import my.annotation.ValidSettlementRatio;

@Getter
@Setter
@ValidSettlementRatio
public class SettlementRatioRequestDto {
    private double ownerRatio;
    private double storeRatio;
}

package my.domain.settlement_ratio.vo;


import lombok.Getter;
import lombok.Setter;
import my.annotation.ValidSettlementRatio;
import my.common.vo.MyApplicationVO;

import java.sql.Timestamp;

@Getter
@Setter
@ValidSettlementRatio
public class SettlementRatioVO extends MyApplicationVO {
    private double ownerRatio;
    private double storeRatio;
    private Timestamp createdAt;


}

package my.domain.settlement_ratio.service;

import my.domain.settlement_ratio.vo.SettlementRatioVO;

import java.util.List;

public interface SettlementRatioService {

    long setRatio(SettlementRatioVO settlementRatioVO);
    SettlementRatioVO getRatio();
    List<SettlementRatioVO> getRatioHistory();

}

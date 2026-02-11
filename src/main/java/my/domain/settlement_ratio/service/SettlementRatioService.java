package my.domain.settlement_ratio.service;

import my.domain.settlement_ratio.vo.SettlementRatioVO;

import java.util.List;

public interface SettlementRatioService {

    long create(SettlementRatioVO settlementRatioVO);
    SettlementRatioVO findCurrentRatio();
    List<SettlementRatioVO> findAll();

}

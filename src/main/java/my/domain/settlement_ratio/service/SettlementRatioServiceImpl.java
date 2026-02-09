package my.domain.settlement_ratio.service;
import lombok.RequiredArgsConstructor;
import my.common.exception.ErrorCode;
import my.common.exception.SettlementInsertFailException;
import my.domain.settlement_ratio.SettlementRatioMapper;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementRatioServiceImpl implements SettlementRatioService {


    private final SettlementRatioMapper settlementRatioMapper;



    @Override
    public long setRatio(SettlementRatioVO settlementRatioVO) {

        int result = settlementRatioMapper.insertNewRatio(settlementRatioVO);
        if (result != 1) throw new SettlementInsertFailException(ErrorCode.SETTLEMENT_RATIO_INSERT_FAIL);

        return settlementRatioVO.getId();
    }

    @Override
    public SettlementRatioVO getRatio() {
        return settlementRatioMapper.selectCurrentRatio();
    }

    @Override
    public List<SettlementRatioVO> getRatioHistory() {
        return settlementRatioMapper.selectAllRatios();
    }
}

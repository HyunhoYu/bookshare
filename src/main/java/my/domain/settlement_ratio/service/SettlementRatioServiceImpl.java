package my.domain.settlement_ratio.service;
import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.settlement_ratio.SettlementRatioMapper;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementRatioServiceImpl implements SettlementRatioService {


    private final SettlementRatioMapper settlementRatioMapper;



    @Override
    @Transactional
    public long create(SettlementRatioVO settlementRatioVO) {

        int result = settlementRatioMapper.insert(settlementRatioVO);
        if (result != 1) throw new ApplicationException(ErrorCode.SETTLEMENT_RATIO_INSERT_FAIL);

        return settlementRatioVO.getId();
    }

    @Override
    public SettlementRatioVO findCurrentRatio() {
        return settlementRatioMapper.selectCurrent();
    }

    @Override
    public List<SettlementRatioVO> findAll() {
        return settlementRatioMapper.selectAll();
    }
}

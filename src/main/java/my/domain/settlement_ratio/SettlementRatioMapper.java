package my.domain.settlement_ratio;


import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettlementRatioMapper {

    int insertNewRatio(SettlementRatioVO settlementRatioVO);
    SettlementRatioVO selectCurrentRatio();
    List<SettlementRatioVO> selectAllRatios();
    SettlementRatioVO selectById(long id);
}

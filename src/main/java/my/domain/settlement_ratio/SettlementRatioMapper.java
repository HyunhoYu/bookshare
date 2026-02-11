package my.domain.settlement_ratio;


import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettlementRatioMapper {

    int insert(SettlementRatioVO settlementRatioVO);
    SettlementRatioVO selectCurrent();
    List<SettlementRatioVO> selectAll();
    SettlementRatioVO selectById(long id);
}

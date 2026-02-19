package my.domain.settlement;

import my.domain.settlement.vo.SettlementRentalOffsetVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettlementRentalOffsetMapper {
    int insert(SettlementRentalOffsetVO vo);
    List<SettlementRentalOffsetVO> selectBySettlementId(Long settlementId);
    List<SettlementRentalOffsetVO> selectByRentalSettlementId(Long rentalSettlementId);
}

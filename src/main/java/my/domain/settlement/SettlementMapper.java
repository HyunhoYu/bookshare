package my.domain.settlement;

import my.domain.settlement.vo.SettlementVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettlementMapper {

    List<SettlementVO> selectAllByBookOwnerId(Long bookOwnerId);
    List<SettlementVO> selectSettledByBookOwnerId(Long bookOwnerId);
    int insert(SettlementVO settlementVO);
}

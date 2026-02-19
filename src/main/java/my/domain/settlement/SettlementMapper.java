package my.domain.settlement;

import my.domain.settlement.vo.SettlementVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettlementMapper {

    List<SettlementVO> selectAll();
    List<SettlementVO> selectAllByBookOwnerId(Long bookOwnerId);
    List<SettlementVO> selectSettledByBookOwnerId(Long bookOwnerId);
    SettlementVO selectById(Long id);
    int insert(SettlementVO settlementVO);
}

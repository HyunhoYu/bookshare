package my.domain.deposit;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepositRentalOffsetMapper {

    int insert(DepositRentalOffsetVO vo);
    List<DepositRentalOffsetVO> selectByDepositId(Long depositId);
}

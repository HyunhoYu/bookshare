package my.domain.deposit;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepositRentalOffsetMapper {

    int insert(DepositRentalOffsetVO vo);
}

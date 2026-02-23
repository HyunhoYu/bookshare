package my.domain.deposit;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepositMapper {

    int insert(DepositVO vo);
    DepositVO selectByBookOwnerId(Long bookOwnerId);
    int update(DepositVO vo);
}

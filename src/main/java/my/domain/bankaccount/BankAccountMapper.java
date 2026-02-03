package my.domain.bankaccount;

import my.domain.bankaccount.vo.BankAccountVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankAccountMapper {

    int insert(BankAccountVO bankAccountVO);

    BankAccountVO selectById(Long id);
}

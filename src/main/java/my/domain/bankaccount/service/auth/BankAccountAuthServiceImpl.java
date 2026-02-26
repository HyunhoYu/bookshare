package my.domain.bankaccount.service.auth;

import lombok.RequiredArgsConstructor;
import my.common.util.BankCodeResolver;
import my.domain.bankaccount.BankAccountMapper;
import my.domain.bankaccount.vo.BankAccountVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BankAccountAuthServiceImpl implements BankAccountAuthService{

    private final BankAccountMapper bankAccountMapper;

    @Override
    @Transactional
    public int save(BankAccountVO bankAccountVO) {
        if (bankAccountVO.getBankCode() == null && bankAccountVO.getBankName() != null) {
            bankAccountVO.setBankCode(BankCodeResolver.resolve(bankAccountVO.getBankName()));
        }
        return bankAccountMapper.insert(bankAccountVO);
    }

}

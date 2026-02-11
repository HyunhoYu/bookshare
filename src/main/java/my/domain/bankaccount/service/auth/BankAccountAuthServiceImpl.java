package my.domain.bankaccount.service.auth;

import lombok.RequiredArgsConstructor;
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
        return bankAccountMapper.insert(bankAccountVO);
    }

}

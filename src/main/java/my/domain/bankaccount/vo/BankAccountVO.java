package my.domain.bankaccount.vo;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;

@Getter
@Setter
public class BankAccountVO extends MyApplicationVO {


    private String accountNumber;
    private String bankName;
}

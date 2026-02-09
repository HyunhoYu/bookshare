package my.domain.bookowner.vo;

import lombok.Getter;
import lombok.Setter;
import my.domain.bankaccount.vo.BankAccountVO;
import my.domain.user.UserVO;

@Getter
@Setter
public class BookOwnerVO extends UserVO {

    private BankAccountVO bankAccountVO;
}

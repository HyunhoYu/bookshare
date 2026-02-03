package my.domain.bookowner.vo;

import lombok.Getter;
import lombok.Setter;
import my.domain.bankaccount.vo.BankAccountVO;
import my.domain.user.UserVO;
import my.utils.annotation.Ref;

@Getter
@Setter
public class BookOwnerVO extends UserVO {

    private Long id;
    @Ref(reference = UserVO.class)
    private UserVO userVO;
    private BankAccountVO bankAccountVO;

}

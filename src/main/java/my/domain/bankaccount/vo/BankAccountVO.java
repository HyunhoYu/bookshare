package my.domain.bankaccount.vo;

import lombok.Getter;
import lombok.Setter;
import my.common.vo.MyApplicationVO;
import my.domain.bookowner.vo.BookOwnerVO;
import my.utils.annotation.Ref;

@Getter
@Setter
public class BankAccountVO extends MyApplicationVO {

    @Ref(reference = BookOwnerVO.class)
    private Long id;
    private String accountNumber;
    private String bankName;
}

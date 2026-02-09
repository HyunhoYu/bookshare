package my.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TossTransferRequestDto {

    private String bankCode;
    private String accountNumber;
    private int amount;
    private String holderName;
}

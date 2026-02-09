package my.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossTransferResponseDto {

    private String payoutKey;
    private String status;
    private String bankCode;
    private String accountNumber;
    private int amount;
    private String failureCode;
    private String failureMessage;
}

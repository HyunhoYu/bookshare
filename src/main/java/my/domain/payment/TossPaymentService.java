package my.domain.payment;

import my.domain.payment.dto.TossTransferResponseDto;

public interface TossPaymentService {

    TossTransferResponseDto transfer(String bankCode, String accountNumber, int amount, String holderName);
}

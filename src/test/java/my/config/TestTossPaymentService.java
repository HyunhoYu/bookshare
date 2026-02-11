package my.config;

import my.domain.payment.TossPaymentService;
import my.domain.payment.dto.TossTransferResponseDto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Primary
@Service
public class TestTossPaymentService implements TossPaymentService {

    @Override
    public TossTransferResponseDto transfer(String bankCode, String accountNumber, int amount, String holderName) {
        TossTransferResponseDto response = new TossTransferResponseDto();
        response.setPayoutKey("test_payout_" + UUID.randomUUID().toString().substring(0, 8));
        response.setStatus("COMPLETED");
        response.setBankCode(bankCode);
        response.setAccountNumber(accountNumber);
        response.setAmount(amount);
        return response;
    }
}

package my.domain.payment;

import lombok.extern.slf4j.Slf4j;
import my.domain.payment.dto.TossTransferResponseDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Profile("dev")
@Slf4j
@Service
public class MockTossPaymentService implements TossPaymentService {

    @Override
    public TossTransferResponseDto transfer(String bankCode, String accountNumber, int amount, String holderName) {
        log.info("Mock 송금 처리 - bankCode: {}, accountNumber: {}, amount: {}, holderName: {}",
                bankCode, accountNumber, amount, holderName);

        TossTransferResponseDto response = new TossTransferResponseDto();
        response.setPayoutKey("mock_payout_" + UUID.randomUUID().toString().substring(0, 8));
        response.setStatus("COMPLETED");
        response.setBankCode(bankCode);
        response.setAccountNumber(accountNumber);
        response.setAmount(amount);
        return response;
    }
}

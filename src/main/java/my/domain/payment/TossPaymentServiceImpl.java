package my.domain.payment;

import lombok.extern.slf4j.Slf4j;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.payment.dto.TossTransferRequestDto;
import my.domain.payment.dto.TossTransferResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class TossPaymentServiceImpl implements TossPaymentService {

    private final RestTemplate restTemplate;
    private final String secretKey;
    private final String baseUrl;

    public TossPaymentServiceImpl(
            @Value("${toss.payments.secret-key}") String secretKey,
            @Value("${toss.payments.base-url}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public TossTransferResponseDto transfer(String bankCode, String accountNumber, int amount, String holderName) {
        String url = baseUrl + "/payouts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodeSecretKey());

        TossTransferRequestDto requestDto = new TossTransferRequestDto(bankCode, accountNumber, amount, holderName);
        HttpEntity<TossTransferRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<TossTransferResponseDto> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, TossTransferResponseDto.class
            );

            log.info("토스 송금 성공 - bankCode: {}, amount: {}", bankCode, amount);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("토스 송금 실패 - bankCode: {}, accountNumber: {}, amount: {}, error: {}",
                    bankCode, accountNumber, amount, e.getMessage());
            throw new ApplicationException(ErrorCode.SETTLEMENT_TRANSFER_FAIL);
        }
    }

    private String encodeSecretKey() {
        String raw = secretKey + ":";
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}

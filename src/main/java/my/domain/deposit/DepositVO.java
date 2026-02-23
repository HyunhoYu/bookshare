package my.domain.deposit;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DepositVO {
    private Long id;
    private Long bookOwnerId;
    private Integer amount;
    private Integer remainingAmount;
    private String status;
    private LocalDateTime createdAt;
}

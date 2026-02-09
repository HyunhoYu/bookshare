package my.domain.settlement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SettlementRequestDto {
    private Long bookOwnerId;
    private List<Long> saleRecordIds;
}

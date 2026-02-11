package my.domain.settlement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SettlementRequestDto {
    @NotNull(message = "책 소유주 ID는 필수입니다")
    private Long bookOwnerId;

    @NotEmpty(message = "판매기록 목록은 비어있을 수 없습니다")
    private List<Long> saleRecordIds;
}

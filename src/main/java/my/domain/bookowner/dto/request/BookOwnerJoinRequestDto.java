package my.domain.bookowner.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import my.domain.user.dto.request.UserJoinRequestDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BookOwnerJoinRequestDto extends UserJoinRequestDto {

    // 계좌 정보
    private String bankName;          // 은행명
    private String accountNumber;     // 계좌번호
}

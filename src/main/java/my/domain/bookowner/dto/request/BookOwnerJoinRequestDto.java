package my.domain.bookowner.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "은행명은 필수입니다")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수입니다")
    private String accountNumber;
}

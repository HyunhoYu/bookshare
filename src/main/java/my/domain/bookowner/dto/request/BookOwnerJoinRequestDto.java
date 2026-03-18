package my.domain.bookowner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "은행명은 50자 이내여야 합니다")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수입니다")
    @Size(min = 10, max = 50, message = "계좌번호는 10~50자여야 합니다")
    @Pattern(regexp = "^[\\d-]+$", message = "계좌번호는 숫자와 하이픈만 허용됩니다")
    private String accountNumber;
}

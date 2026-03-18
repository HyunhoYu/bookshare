package my.domain.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUpdateDto {

    @Size(max = 100, message = "이름은 100자 이내여야 합니다")
    private String name;

    @Size(max = 20, message = "전화번호는 20자 이내여야 합니다")
    private String phone;

    // Address fields
    private String city;
    private String loadAddr;
    private String specificAddr;
}

package my.domain.user.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserJoinRequestDto {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "주민번호는 필수입니다")
    private String residentNumber;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    private String city;
    private String loadAddr;
    private String specificAddr;
}

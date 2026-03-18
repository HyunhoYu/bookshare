package my.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
    private String name;

    @NotBlank(message = "주민번호는 필수입니다")
    @Pattern(regexp = "^\\d{6}-?\\d{7}$", message = "주민번호 형식이 올바르지 않습니다 (예: 990101-1234567)")
    private String residentNumber;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    private String phone;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Size(max = 100, message = "이메일은 100자 이내여야 합니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다")
    private String password;

    @Size(max = 100, message = "도시는 100자 이내여야 합니다")
    private String city;

    @Size(max = 200, message = "도로명 주소는 200자 이내여야 합니다")
    private String loadAddr;

    @Size(max = 200, message = "상세 주소는 200자 이내여야 합니다")
    private String specificAddr;
}

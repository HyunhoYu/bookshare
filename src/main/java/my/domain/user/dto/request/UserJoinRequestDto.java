package my.domain.user.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserJoinRequestDto {

    private String name;              // 이름
    private String residentNumber;    // 주민번호
    private String phone;             // 전화번호
    private String address;           // 주소
    private String email;             // 이메일
    private String password;          // 비밀번호

}

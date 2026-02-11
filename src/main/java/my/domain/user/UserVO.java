package my.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import my.common.vo.MyApplicationVO;
import my.enums.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO extends MyApplicationVO{
    private Role role;
    private String name;
    private String phone;
    private String email;
    @JsonIgnore
    private String residentNumber;
    @JsonIgnore
    private String password;
}

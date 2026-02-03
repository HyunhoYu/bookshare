package my.domain.user;

import lombok.*;
import my.common.vo.MyApplicationVO;
import my.enums.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO extends MyApplicationVO{
    private Long id;
    private Role role;
    private String name;
    private String phone;
    private String email;
    private String residentNumber;
    private String password;
}

package my.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String password;

}

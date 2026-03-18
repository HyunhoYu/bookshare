package my.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import my.enums.Role;

@Getter
@Builder
public class MyAccountDto {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Role role;
    // Address
    private String city;
    private String loadAddr;
    private String specificAddr;
}

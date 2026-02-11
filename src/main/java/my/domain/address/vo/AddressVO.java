package my.domain.address.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressVO {

    private Long userId;
    private String city;
    private String loadAddr;
    private String specificAddr;
}

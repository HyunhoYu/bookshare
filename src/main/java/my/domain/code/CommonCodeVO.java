package my.domain.code;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonCodeVO extends GroupCodeVO {
    private String code;
    private String codeName;
}

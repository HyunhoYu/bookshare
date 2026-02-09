package my.domain.bookcasetype;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseTypeVO {
    private Long id;
    private String code;
    private Integer monthlyPrice;
    private LocalDateTime createdAt;
}

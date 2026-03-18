package my.domain.bookcase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseVO {
    private Long id;
    private String groupCodeId;
    private String commonCodeId;
    private Long bookCaseTypeId;
    private String locationName;
    private String bookCaseTypeCode;
    private Integer monthlyPrice;
    private Boolean occupied;
    private Long bookOwnerId;
}

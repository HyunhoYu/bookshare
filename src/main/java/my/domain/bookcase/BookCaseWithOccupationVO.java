package my.domain.bookcase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseWithOccupationVO {
    private Long id;
    private String groupCodeId;
    private String commonCodeId;
    private String locationName;
    private Long bookCaseTypeId;
    private String bookCaseTypeCode;
    private Integer monthlyPrice;
    private boolean occupied;
    private Long bookOwnerId;
}

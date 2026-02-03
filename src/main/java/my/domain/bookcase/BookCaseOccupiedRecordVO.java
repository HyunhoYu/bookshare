package my.domain.bookcase;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCaseOccupiedRecordVO {
    private Long id;
    private Long bookCaseId;
    private Long bookOwnerId;
    private LocalDateTime occupiedAt;
    private LocalDateTime unOccupiedAt;
}

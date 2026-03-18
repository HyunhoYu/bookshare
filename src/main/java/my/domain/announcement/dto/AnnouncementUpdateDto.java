package my.domain.announcement.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementUpdateDto {

    @Size(max = 200, message = "제목은 200자 이내여야 합니다")
    private String title;

    @Size(max = 4000, message = "내용은 4000자 이내여야 합니다")
    private String content;

    private String targetRole;

    private Boolean pinned;
}

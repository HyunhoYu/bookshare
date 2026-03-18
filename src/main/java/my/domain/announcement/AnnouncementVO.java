package my.domain.announcement;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AnnouncementVO {
    private Long id;
    private Long adminId;
    private String title;
    private String content;
    private String targetRole; // ALL, CUSTOMER, BOOK_OWNER
    private int isPinned; // 0 or 1
    private int viewCount;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;

    // Joined field
    private String adminName;
}

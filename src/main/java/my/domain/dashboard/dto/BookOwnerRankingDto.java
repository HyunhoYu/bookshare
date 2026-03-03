package my.domain.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookOwnerRankingDto {
    private Long bookOwnerId;
    private String bookOwnerName;
    private String nickname;
    private int followerCount;
    private long totalSalesAmount;
    private int soldCount;
}

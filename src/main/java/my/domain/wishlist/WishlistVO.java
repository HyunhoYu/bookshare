package my.domain.wishlist;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WishlistVO {
    private Long id;
    private Long customerId;
    private Long bookId;
    private Date createdAt;

    // Joined fields for list display
    private String bookName;
    private String publisherHouse;
    private int price;
    private String genreName;
    private String locationName;
    private String bookOwnerName;
    private String bookOwnerNickname;
    private Long bookOwnerId;
}

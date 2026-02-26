package my.domain.bookowner_profile;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class BookOwnerProfileVO {
    private Long bookOwnerId;
    private String nickname;
    private String favoriteBooks;
    private String favoriteAuthors;
    private String favoriteGenres;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // JOIN fields
    private String bookOwnerName;
}

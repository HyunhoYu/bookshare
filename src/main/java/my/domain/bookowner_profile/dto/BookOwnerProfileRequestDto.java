package my.domain.bookowner_profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookOwnerProfileRequestDto {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 50, message = "닉네임은 50자 이내여야 합니다")
    private String nickname;

    @Size(max = 500, message = "좋아하는 책은 500자 이내여야 합니다")
    private String favoriteBooks;

    @Size(max = 500, message = "좋아하는 작가는 500자 이내여야 합니다")
    private String favoriteAuthors;

    @Size(max = 500, message = "좋아하는 장르는 500자 이내여야 합니다")
    private String favoriteGenres;
}

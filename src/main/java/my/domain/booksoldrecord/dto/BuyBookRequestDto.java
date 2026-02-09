package my.domain.booksoldrecord.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyBookRequestDto {

    private Long bookId;
    private Long customerId;
    private String buyTypeCommonCode;


}

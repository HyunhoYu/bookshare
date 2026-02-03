package my.domain.bookowner.service.auth;

import my.domain.bookowner.dto.BookOwnerJoinRequestDto;
import my.domain.bookowner.vo.BookOwnerVO;

public interface BookOwnerAuthService {

    int save(BookOwnerVO bookOwnerVO);

    BookOwnerVO signup(BookOwnerJoinRequestDto dto);
}

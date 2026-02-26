package my.domain.bookowner_profile.service;

import my.domain.bookowner_profile.BookOwnerProfileVO;
import my.domain.bookowner_profile.dto.BookOwnerProfileRequestDto;

public interface BookOwnerProfileService {
    BookOwnerProfileVO create(Long bookOwnerId, BookOwnerProfileRequestDto dto);
    BookOwnerProfileVO update(Long bookOwnerId, BookOwnerProfileRequestDto dto);
    BookOwnerProfileVO findByBookOwnerId(Long bookOwnerId);
}

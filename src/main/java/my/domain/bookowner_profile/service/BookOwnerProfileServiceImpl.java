package my.domain.bookowner_profile.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner_profile.BookOwnerProfileMapper;
import my.domain.bookowner_profile.BookOwnerProfileVO;
import my.domain.bookowner_profile.dto.BookOwnerProfileRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookOwnerProfileServiceImpl implements BookOwnerProfileService {

    private final BookOwnerProfileMapper profileMapper;
    private final BookOwnerMapper bookOwnerMapper;

    @Override
    @Transactional
    public BookOwnerProfileVO create(Long bookOwnerId, BookOwnerProfileRequestDto dto) {
        requireNonNull(bookOwnerMapper.selectById(bookOwnerId), ErrorCode.BOOK_OWNER_NOT_FOUND);

        if (profileMapper.selectByBookOwnerId(bookOwnerId) != null) {
            throw new ApplicationException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        if (profileMapper.selectByNickname(dto.getNickname()) != null) {
            throw new ApplicationException(ErrorCode.DUPLICATE_NICKNAME);
        }

        BookOwnerProfileVO vo = new BookOwnerProfileVO();
        vo.setBookOwnerId(bookOwnerId);
        vo.setNickname(dto.getNickname());
        vo.setFavoriteBooks(dto.getFavoriteBooks());
        vo.setFavoriteAuthors(dto.getFavoriteAuthors());
        vo.setFavoriteGenres(dto.getFavoriteGenres());

        int result = profileMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.PROFILE_INSERT_FAIL);
        }

        return profileMapper.selectByBookOwnerId(bookOwnerId);
    }

    @Override
    @Transactional
    public BookOwnerProfileVO update(Long bookOwnerId, BookOwnerProfileRequestDto dto) {
        BookOwnerProfileVO existing = requireNonNull(
                profileMapper.selectByBookOwnerId(bookOwnerId),
                ErrorCode.PROFILE_NOT_FOUND
        );

        if (!existing.getNickname().equals(dto.getNickname())) {
            BookOwnerProfileVO byNickname = profileMapper.selectByNickname(dto.getNickname());
            if (byNickname != null) {
                throw new ApplicationException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }

        existing.setNickname(dto.getNickname());
        existing.setFavoriteBooks(dto.getFavoriteBooks());
        existing.setFavoriteAuthors(dto.getFavoriteAuthors());
        existing.setFavoriteGenres(dto.getFavoriteGenres());

        profileMapper.update(existing);

        return profileMapper.selectByBookOwnerId(bookOwnerId);
    }

    @Override
    public BookOwnerProfileVO findByBookOwnerId(Long bookOwnerId) {
        return profileMapper.selectByBookOwnerId(bookOwnerId);
    }
}

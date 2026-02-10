package my.domain.bookcase.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.*;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.bookcase.*;
import my.domain.bookcasetype.BookCaseTypeMapper;
import my.domain.bookcasetype.BookCaseTypeVO;
import my.domain.code.CommonCodeMapper;
import my.domain.code.CommonCodeVO;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCaseServiceImpl implements BookCaseService {

    private final BookCaseMapper bookCaseMapper;
    private final BookCaseTypeMapper bookCaseTypeMapper;
    private final BookCaseOccupiedRecordMapper occupiedRecordMapper;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final CommonCodeMapper commonCodeMapper;

    @Override
    public long addBookCase(BookCaseVO bookCaseVO) {
        // 책장 타입이 실제로 존재하는지 검증
        BookCaseTypeVO bookCaseType = bookCaseTypeMapper.selectById(bookCaseVO.getBookCaseTypeId());
        if (bookCaseType == null) {
            throw new BookCaseTypeNotFoundException(ErrorCode.BOOK_CASE_TYPE_NOT_FOUND);
        }

        int result = bookCaseMapper.insertBookCase(bookCaseVO);
        if (result != 1) {
            throw new RuntimeException("책장 저장 실패");
        }
        return bookCaseVO.getId();
    }

    @Override
    public BookCaseVO findById(long id) {
        return bookCaseMapper.selectById(id);
    }

    @Override
    public List<BookCaseVO> findAll() {
        return bookCaseMapper.selectAll();
    }

    @Override
    public List<BookCaseVO> findUsableBookCases() {
        return bookCaseMapper.selectUsableBookCases();
    }


    @Override
    public boolean isOccupied(long bookCaseId) {
        return occupiedRecordMapper.countCurrentOccupied(bookCaseId) > 0;
    }


    @Override
    @Transactional
    public BookCaseOccupiedRecordVO occupy(long bookOwnerId, long bookCaseId) {
        BookCaseVO bookCase = bookCaseMapper.selectById(bookCaseId);
        if (bookCase == null) {
            throw new BookCaseNotFoundException(ErrorCode.BOOK_CASE_NOT_FOUND);
        }

        if (isOccupied(bookCaseId)) {
            throw new BookCaseAlreadyOccupiedException(ErrorCode.BOOK_CASE_ALREADY_OCCUPIED);
        }

        BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
        record.setBookOwnerId(bookOwnerId);
        record.setBookCaseId(bookCaseId);

        occupiedRecordMapper.insert(record);
        return occupiedRecordMapper.selectById(record.getId());
    }

    @Override
    @Transactional
    public List<BookVO> registerBooks(long bookCaseId, List<BookRegisterDto> bookRegisterDtos) {
        validateBookCaseExists(bookCaseId);
        UserVO bookOwner = findBookOwnerByNameAndPhone(bookRegisterDtos);
        validateOwnerOccupiesBookCase(bookCaseId, bookOwner.getId());

        return bookRegisterDtos.stream()
                .map(dto -> createBook(dto, bookOwner.getId(), bookCaseId))
                .toList();
    }

    private void validateBookCaseExists(long bookCaseId) {
        if (bookCaseMapper.selectById(bookCaseId) == null) {
            throw new BookCaseNotFoundException(ErrorCode.BOOK_CASE_NOT_FOUND);
        }
    }

    private void validateOwnerOccupiesBookCase(long bookCaseId, Long bookOwnerId) {
        BookCaseOccupiedRecordVO record = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
        if (record == null || !record.getBookOwnerId().equals(bookOwnerId)) {
            throw new ForbiddenException(ErrorCode.BOOK_CASE_NOT_OCCUPIED_BY_OWNER);
        }
    }

    private BookVO createBook(BookRegisterDto dto, Long bookOwnerId, long bookCaseId) {
        String commonCodeId = resolveBookTypeCode(dto.getBookType());

        BookVO bookVO = new BookVO();
        bookVO.setBookOwnerId(bookOwnerId);
        bookVO.setBookCaseId(bookCaseId);
        bookVO.setBookName(dto.getBookName());
        bookVO.setPublisherHouse(dto.getPublisherHouse());
        bookVO.setPrice(dto.getPrice());
        bookVO.setCommonCodeId(commonCodeId);

        int result = bookMapper.insertBook(bookVO);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.BOOK_INSERT_FAIL);
        }

        return bookMapper.selectById(bookVO.getId());
    }

    private String resolveBookTypeCode(String bookTypeName) {
        CommonCodeVO codeVO = commonCodeMapper.selectByGroupCodeAndCodeName("BOOK_TYPE", bookTypeName);
        if (codeVO == null) {
            throw new ApplicationException(ErrorCode.INVALID_BOOK_TYPE);
        }
        return codeVO.getCode();
    }

    private UserVO findBookOwnerByNameAndPhone(List<BookRegisterDto> bookRegisterDtos) {
        String name = bookRegisterDtos.get(0).getUserName();
        String phone = bookRegisterDtos.get(0).getUserPhone();

        boolean allSameOwner = bookRegisterDtos.stream()
                .allMatch(dto -> dto.getUserName().equals(name) && dto.getUserPhone().equals(phone));

        if (!allSameOwner) {
            throw new BookOwnerMismatchException(ErrorCode.BOOK_OWNER_MISMATCH);
        }

        return userMapper.selectBookOwnerByNameAndPhone(name, phone);
    }

    @Override
    public List<Long> selectMyOccupyingBookCasesByBookOwnerId(Long bookOwnerId) {
        return bookCaseMapper.selectMyOccupyingBookCasesByBookOwnerId(bookOwnerId);
    }


    @Override
    @Transactional
    public List<Long> unOccupyProcess(List<Long> bookCaseIds) {
        validateBookCaseIdsNotEmpty(bookCaseIds);

        for (Long bookCaseId : bookCaseIds) {
            validateBookCaseExists(bookCaseId);
            validateCurrentlyOccupied(occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId));
        }

        int result = occupiedRecordMapper.unOccupyBookCases(bookCaseIds);
        validateUnoccupyResult(result, bookCaseIds.size());

        List<Long> bookIds = bookMapper.selectNormalBookIdsByBookCaseIds(bookCaseIds);
        if (!bookIds.isEmpty()) {
            bookMapper.updateStateNormalToRetrieve(bookIds);
        }

        return bookIds;
    }

    private void validateBookCaseIdsNotEmpty(List<Long> bookCaseIds) {
        if (bookCaseIds == null || bookCaseIds.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_UNOCCUPY_REQUEST);
        }
    }

    private void validateCurrentlyOccupied(BookCaseOccupiedRecordVO record) {
        if (record == null) {
            throw new ApplicationException(ErrorCode.BOOK_CASE_NOT_OCCUPIED);
        }
    }

    private void validateUnoccupyResult(int result, int expected) {
        if (result != expected) {
            throw new ApplicationException(ErrorCode.UNOCCUPY_FAIL);
        }
    }


}

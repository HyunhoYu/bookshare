package my.domain.bookcase.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.bookcase.*;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.bookcasetype.BookCaseTypeMapper;
import my.domain.deposit.DepositMapper;
import my.domain.deposit.DepositVO;
import my.domain.bookcasetype.BookCaseTypeVO;
import my.domain.code.CommonCodeMapper;
import my.domain.rental.service.RentalSettlementService;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final RentalSettlementService rentalSettlementService;
    private final BookSoldRecordMapper bookSoldRecordMapper;
    private final DepositMapper depositMapper;

    @Override
    public long create(BookCaseCreateDto dto) {
        requireNonNull(bookCaseTypeMapper.selectById(dto.getBookCaseTypeId()), ErrorCode.BOOK_CASE_TYPE_NOT_FOUND);
        validateLocationCode(dto.getLocationCode());

        BookCaseVO bookCaseVO = new BookCaseVO();
        bookCaseVO.setGroupCodeId("LOCATION");
        bookCaseVO.setCommonCodeId(dto.getLocationCode());
        bookCaseVO.setBookCaseTypeId(dto.getBookCaseTypeId());

        int result = bookCaseMapper.insert(bookCaseVO);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.BOOK_CASE_INSERT_FAIL);
        }
        return bookCaseVO.getId();
    }

    private void validateLocationCode(String locationCode) {
        requireNonNull(
                commonCodeMapper.selectByGroupCodeAndCode("LOCATION", locationCode),
                ErrorCode.INVALID_LOCATION_CODE);
    }

    @Override
    public BookCaseVO findById(Long id) {
        return bookCaseMapper.selectById(id);
    }

    @Override
    public List<BookCaseVO> findAll() {
        return bookCaseMapper.selectAll();
    }

    @Override
    public List<BookCaseVO> findUsable() {
        return bookCaseMapper.selectUsableBookCases();
    }


    @Override
    public boolean isOccupied(Long bookCaseId) {
        return occupiedRecordMapper.countCurrentOccupied(bookCaseId) > 0;
    }


    @Override
    @Transactional
    public List<BookCaseOccupiedRecordVO> occupy(Long bookOwnerId, List<Long> bookCaseIds, LocalDate expirationDate, int depositAmount) {
        List<BookCaseOccupiedRecordVO> results = new ArrayList<>();

        for (Long bookCaseId : bookCaseIds) {
            validateBookCaseExists(bookCaseId);

            if (isOccupied(bookCaseId)) {
                throw new ApplicationException(ErrorCode.BOOK_CASE_ALREADY_OCCUPIED);
            }

            BookCaseVO bookCase = bookCaseMapper.selectById(bookCaseId);
            BookCaseTypeVO type = bookCaseTypeMapper.selectById(bookCase.getBookCaseTypeId());
            int monthlyPrice = type.getMonthlyPrice();

            BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
            record.setBookOwnerId(bookOwnerId);
            record.setBookCaseId(bookCaseId);
            record.setExpirationDate(expirationDate);

            occupiedRecordMapper.insert(record);
            rentalSettlementService.generateSettlements(record.getId(), bookOwnerId, LocalDate.now(), expirationDate, monthlyPrice);
            results.add(occupiedRecordMapper.selectById(record.getId()));
        }

        DepositVO deposit = depositMapper.selectByBookOwnerId(bookOwnerId);
        if (deposit == null) {
            deposit = new DepositVO();
            deposit.setBookOwnerId(bookOwnerId);
            deposit.setAmount(depositAmount);
            deposit.setRemainingAmount(depositAmount);
            deposit.setStatus("HELD");
            if (depositMapper.insert(deposit) != 1) {
                throw new ApplicationException(ErrorCode.DEPOSIT_INSERT_FAIL);
            }
        } else {
            deposit.setAmount(deposit.getAmount() + depositAmount);
            deposit.setRemainingAmount(deposit.getRemainingAmount() + depositAmount);
            depositMapper.update(deposit);
        }

        return results;
    }

    @Override
    @Transactional
    public List<BookVO> registerBooks(Long bookCaseId, List<BookRegisterDto> bookRegisterDtos) {
        validateBookCaseExists(bookCaseId);
        UserVO bookOwner = findBookOwnerByNameAndPhone(bookRegisterDtos);
        validateOwnerOccupiesBookCase(bookCaseId, bookOwner.getId());

        return bookRegisterDtos.stream()
                .map(dto -> createBook(dto, bookOwner.getId(), bookCaseId))
                .toList();
    }

    private void validateBookCaseExists(Long bookCaseId) {
        requireNonNull(bookCaseMapper.selectById(bookCaseId), ErrorCode.BOOK_CASE_NOT_FOUND);
    }

    private void validateOwnerOccupiesBookCase(Long bookCaseId, Long bookOwnerId) {
        BookCaseOccupiedRecordVO record = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
        if (record == null || !record.getBookOwnerId().equals(bookOwnerId)) {
            throw new ApplicationException(ErrorCode.BOOK_CASE_NOT_OCCUPIED_BY_OWNER);
        }
    }

    private BookVO createBook(BookRegisterDto dto, Long bookOwnerId, Long bookCaseId) {
        validateBookTypeCode(dto.getBookTypeCode());

        BookVO bookVO = new BookVO();
        bookVO.setBookOwnerId(bookOwnerId);
        bookVO.setBookCaseId(bookCaseId);
        bookVO.setBookName(dto.getBookName());
        bookVO.setPublisherHouse(dto.getPublisherHouse());
        bookVO.setPrice(dto.getPrice());
        bookVO.setGroupCodeId("BOOK_TYPE");
        bookVO.setCommonCodeId(dto.getBookTypeCode());

        int result = bookMapper.insert(bookVO);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.BOOK_INSERT_FAIL);
        }

        return bookMapper.selectById(bookVO.getId());
    }

    private void validateBookTypeCode(String bookTypeCode) {
        requireNonNull(
                commonCodeMapper.selectByGroupCodeAndCode("BOOK_TYPE", bookTypeCode),
                ErrorCode.INVALID_BOOK_TYPE);
    }

    private UserVO findBookOwnerByNameAndPhone(List<BookRegisterDto> bookRegisterDtos) {
        String name = bookRegisterDtos.get(0).getUserName();
        String phone = bookRegisterDtos.get(0).getUserPhone();

        boolean allSameOwner = bookRegisterDtos.stream()
                .allMatch(dto -> dto.getUserName().equals(name) && dto.getUserPhone().equals(phone));

        if (!allSameOwner) {
            throw new ApplicationException(ErrorCode.BOOK_OWNER_MISMATCH);
        }

        return userMapper.selectBookOwnerByNameAndPhone(name, phone);
    }

    @Override
    public List<Long> findOccupyingBookCaseIds(Long bookOwnerId) {
        return bookCaseMapper.selectMyOccupyingBookCasesByBookOwnerId(bookOwnerId);
    }


    @Override
    @Transactional
    public List<Long> unOccupyProcess(List<Long> bookCaseIds) {
        validateBookCaseIdsNotEmpty(bookCaseIds);

        List<Long> bookOwnerIds = new ArrayList<>();
        for (Long bookCaseId : bookCaseIds) {
            validateBookCaseExists(bookCaseId);
            BookCaseOccupiedRecordVO record = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
            validateCurrentlyOccupied(record);
            validateNoUnsettledSaleRecords(bookCaseId);
            bookOwnerIds.add(record.getBookOwnerId());
        }

        int result = occupiedRecordMapper.unOccupyBookCases(bookCaseIds);
        validateUnoccupyResult(result, bookCaseIds.size());

        List<Long> bookIds = bookMapper.selectNormalBookIdsByBookCaseIds(bookCaseIds);
        if (!bookIds.isEmpty()) {
            bookMapper.updateStateNormalToRetrieve(bookIds);
        }

        // 퇴거 후 활성 점유가 없는 BookOwner의 보증금 반환 처리
        for (Long bookOwnerId : bookOwnerIds.stream().distinct().toList()) {
            returnDepositIfNoActiveOccupation(bookOwnerId);
        }

        return bookIds;
    }

    private void returnDepositIfNoActiveOccupation(Long bookOwnerId) {
        List<Long> remaining = bookCaseMapper.selectMyOccupyingBookCasesByBookOwnerId(bookOwnerId);
        if (remaining.isEmpty()) {
            DepositVO deposit = depositMapper.selectByBookOwnerId(bookOwnerId);
            if (deposit != null && !"RETURNED".equals(deposit.getStatus())) {
                deposit.setStatus("RETURNED");
                depositMapper.update(deposit);
            }
        }
    }



    private void validateBookCaseIdsNotEmpty(List<Long> bookCaseIds) {
        if (bookCaseIds == null || bookCaseIds.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_UNOCCUPY_REQUEST);
        }
    }

    private void validateCurrentlyOccupied(BookCaseOccupiedRecordVO record) {
        requireNonNull(record, ErrorCode.BOOK_CASE_NOT_OCCUPIED);
    }

    private void validateNoUnsettledSaleRecords(Long bookCaseId) {
        int count = bookSoldRecordMapper.countUnsettledByBookCaseId(bookCaseId);
        if (count > 0) {
            throw new ApplicationException(ErrorCode.UNSETTLED_SALE_RECORD_EXISTS);
        }
    }

    private void validateUnoccupyResult(int result, int expected) {
        if (result != expected) {
            throw new ApplicationException(ErrorCode.UNOCCUPY_FAIL);
        }
    }

    @Override
    public List<BookCaseWithOccupationVO> findAllWithOccupation() {
        return bookCaseMapper.selectAllWithOccupation();
    }

}

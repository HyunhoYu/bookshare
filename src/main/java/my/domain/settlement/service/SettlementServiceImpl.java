package my.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bankaccount.BankAccountMapper;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.settlement.SettlementMapper;
import my.domain.settlement.dto.SettlementRequestDto;
import my.domain.settlement.vo.SettlementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService{

    private final SettlementMapper settlementMapper;
    private final BookSoldRecordMapper bookSoldRecordMapper;
    private final BookOwnerMapper bookOwnerMapper;
    private final BankAccountMapper bankAccountMapper;

    @Override
    public List<SettlementVO> findAll(Long bookOwnerId) {
        return settlementMapper.selectAllByBookOwnerId(bookOwnerId);
    }

    @Override
    public List<SettlementVO> findSettled(Long bookOwnerId) {
        return settlementMapper.selectSettledByBookOwnerId(bookOwnerId);
    }

    @Override
    public List<BookSoldRecordVO> findUnSettled(Long bookOwnerId) {
        return bookSoldRecordMapper.selectUnsettledByBookOwnerId(bookOwnerId);
    }

    @Override
    public List<BookSoldRecordVO> findAllUnsettled() {
        return bookSoldRecordMapper.selectUnsettled();
    }

    @Override
    @Transactional
    public SettlementVO settle(SettlementRequestDto requestDto) {
        Long bookOwnerId = requestDto.getBookOwnerId();
        List<Long> saleRecordIds = requestDto.getSaleRecordIds();

        validateSaleRecordIds(saleRecordIds);
        validateBookOwnerExists(bookOwnerId);
        validateBankAccountExists(bookOwnerId);
        validateAllRecordsOwnedBy(saleRecordIds, bookOwnerId);
        validateNotAlreadySettled(saleRecordIds);

        SettlementVO settlementVO = createSettlement(bookOwnerId);
        bookSoldRecordMapper.updateSettlementId(settlementVO.getId(), saleRecordIds);

        return settlementVO;
    }

    private void validateSaleRecordIds(List<Long> saleRecordIds) {
        if (saleRecordIds == null || saleRecordIds.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_SETTLEMENT_REQUEST);
        }
    }

    private void validateBookOwnerExists(Long bookOwnerId) {
        if (bookOwnerMapper.selectById(bookOwnerId) == null) {
            throw new ApplicationException(ErrorCode.BOOK_OWNER_NOT_FOUND);
        }
    }

    private void validateBankAccountExists(Long bookOwnerId) {
        if (bankAccountMapper.selectById(bookOwnerId) == null) {
            throw new ApplicationException(ErrorCode.BOOK_OWNER_BANK_ACCOUNT_NOT_FOUND);
        }
    }

    private void validateAllRecordsOwnedBy(List<Long> saleRecordIds, Long bookOwnerId) {
        int ownerCount = bookSoldRecordMapper.countByIdsAndBookOwnerId(saleRecordIds, bookOwnerId);
        if (ownerCount != saleRecordIds.size()) {
            throw new ApplicationException(ErrorCode.SETTLEMENT_SALE_RECORD_OWNER_MISMATCH);
        }
    }

    private void validateNotAlreadySettled(List<Long> saleRecordIds) {
        int settledCount = bookSoldRecordMapper.countAlreadySettled(saleRecordIds);
        if (settledCount > 0) {
            throw new ApplicationException(ErrorCode.SETTLEMENT_ALREADY_SETTLED);
        }
    }

    private SettlementVO createSettlement(Long bookOwnerId) {
        SettlementVO settlementVO = new SettlementVO();
        settlementVO.setBookOwnerId(bookOwnerId);

        int insertResult = settlementMapper.insert(settlementVO);
        if (insertResult != 1) {
            throw new ApplicationException(ErrorCode.SETTLEMENT_INSERT_FAIL);
        }

        return settlementVO;
    }
}

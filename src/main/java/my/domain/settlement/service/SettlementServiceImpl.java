package my.domain.settlement.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.common.util.BankCodeResolver;
import my.domain.bankaccount.BankAccountMapper;
import my.domain.bankaccount.vo.BankAccountVO;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.payment.TossPaymentService;
import my.domain.payment.dto.TossTransferResponseDto;
import my.domain.settlement.SettlementMapper;
import my.domain.settlement.dto.SettlementRequestDto;
import my.domain.settlement.vo.SettlementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService{

    private final SettlementMapper settlementMapper;
    private final BookSoldRecordMapper bookSoldRecordMapper;
    private final BookOwnerMapper bookOwnerMapper;
    private final BankAccountMapper bankAccountMapper;
    private final TossPaymentService tossPaymentService;

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

        Map<String, Object> amountMap = bookSoldRecordMapper.sumAmountsByIds(saleRecordIds);
        int totalAmount = ((BigDecimal) amountMap.get("totalAmount")).intValue();
        int ownerAmount = ((BigDecimal) amountMap.get("ownerAmount")).intValue();
        int storeAmount = totalAmount - ownerAmount;

        if (totalAmount == 0) {
            throw new ApplicationException(ErrorCode.SETTLEMENT_AMOUNT_ZERO);
        }

        BankAccountVO bankAccount = bankAccountMapper.selectById(bookOwnerId);
        String bankCode = bankAccount.getBankCode();
        if (bankCode == null) {
            bankCode = BankCodeResolver.resolve(bankAccount.getBankName());
        }
        if (bankCode == null) {
            throw new ApplicationException(ErrorCode.BANK_CODE_NOT_FOUND);
        }

        BookOwnerVO bookOwner = bookOwnerMapper.selectById(bookOwnerId);
        String holderName = bookOwner.getName();

        TossTransferResponseDto transferResponse = tossPaymentService.transfer(
                bankCode, bankAccount.getAccountNumber(), ownerAmount, holderName
        );

        log.info("Settlement transfer completed - payoutKey: {}, bookOwnerId: {}, ownerAmount: {}",
                transferResponse.getPayoutKey(), bookOwnerId, ownerAmount);

        SettlementVO settlementVO = createSettlement(bookOwnerId, totalAmount, ownerAmount, storeAmount,
                transferResponse.getPayoutKey(), transferResponse.getStatus());
        bookSoldRecordMapper.updateSettlementId(settlementVO.getId(), saleRecordIds);

        return settlementVO;
    }

    private void validateSaleRecordIds(List<Long> saleRecordIds) {
        if (saleRecordIds == null || saleRecordIds.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_SETTLEMENT_REQUEST);
        }
    }

    private void validateBookOwnerExists(Long bookOwnerId) {
        requireNonNull(bookOwnerMapper.selectById(bookOwnerId), ErrorCode.BOOK_OWNER_NOT_FOUND);
    }

    private void validateBankAccountExists(Long bookOwnerId) {
        requireNonNull(bankAccountMapper.selectById(bookOwnerId), ErrorCode.BOOK_OWNER_BANK_ACCOUNT_NOT_FOUND);
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

    private SettlementVO createSettlement(Long bookOwnerId, int totalAmount, int ownerAmount,
                                          int storeAmount, String payoutKey, String transferStatus) {
        SettlementVO settlementVO = new SettlementVO();
        settlementVO.setBookOwnerId(bookOwnerId);
        settlementVO.setTotalAmount(totalAmount);
        settlementVO.setOwnerAmount(ownerAmount);
        settlementVO.setStoreAmount(storeAmount);
        settlementVO.setPayoutKey(payoutKey);
        settlementVO.setTransferStatus(transferStatus);

        int insertResult = settlementMapper.insert(settlementVO);
        if (insertResult != 1) {
            throw new ApplicationException(ErrorCode.SETTLEMENT_INSERT_FAIL);
        }

        return settlementMapper.selectById(settlementVO.getId());
    }
}

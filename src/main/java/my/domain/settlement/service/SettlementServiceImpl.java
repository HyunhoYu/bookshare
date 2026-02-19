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
import my.domain.rental.RentalSettlementMapper;
import my.domain.rental.RentalSettlementVO;
import my.domain.settlement.SettlementMapper;
import my.domain.settlement.SettlementRentalOffsetMapper;
import my.domain.settlement.dto.SettlementRequestDto;
import my.domain.settlement.vo.SettlementRentalOffsetVO;
import my.domain.settlement.vo.SettlementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final RentalSettlementMapper rentalSettlementMapper;
    private final SettlementRentalOffsetMapper settlementRentalOffsetMapper;

    @Override
    public List<SettlementVO> findAll() {
        return settlementMapper.selectAll();
    }

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

        // 상계 계산: 미납 임대료 FIFO 공제
        List<RentalSettlementVO> unpaidRentals = rentalSettlementMapper.selectUnpaidByBookOwnerId(bookOwnerId);
        int remainingPayout = ownerAmount;
        int totalDeducted = 0;
        List<DeductionEntry> deductions = new ArrayList<>();

        for (RentalSettlementVO rental : unpaidRentals) {
            if (remainingPayout <= 0) break;
            int deductible = Math.min(rental.getRemainingAmount(), remainingPayout);
            int newRemaining = rental.getRemainingAmount() - deductible;
            int newDeducted = rental.getDeductedAmount() + deductible;
            String newStatus = (newRemaining == 0) ? "PAID" : "UNPAID";
            deductions.add(new DeductionEntry(rental.getId(), newDeducted, newRemaining, newStatus, deductible));
            totalDeducted += deductible;
            remainingPayout -= deductible;
        }

        int actualPayoutAmount = ownerAmount - totalDeducted;

        // 송금: actualPayoutAmount > 0일 때만 토스 송금 호출
        String payoutKey;
        String transferStatus;

        if (actualPayoutAmount > 0) {
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
                    bankCode, bankAccount.getAccountNumber(), actualPayoutAmount, holderName
            );

            payoutKey = transferResponse.getPayoutKey();
            transferStatus = transferResponse.getStatus();

            log.info("정산 송금 완료 - payoutKey: {}, bookOwnerId: {}, ownerAmount: {}, deducted: {}, actualPayout: {}",
                    payoutKey, bookOwnerId, ownerAmount, totalDeducted, actualPayoutAmount);
        } else {
            payoutKey = "RENTAL_OFFSET";
            transferStatus = "OFFSET_COMPLETED";

            log.info("정산 전액 상계 - bookOwnerId: {}, ownerAmount: {}, deducted: {}, actualPayout: 0",
                    bookOwnerId, ownerAmount, totalDeducted);
        }

        // Settlement INSERT
        SettlementVO settlementVO = createSettlement(bookOwnerId, totalAmount, ownerAmount, storeAmount,
                payoutKey, transferStatus, totalDeducted, actualPayoutAmount);

        // 판매기록 UPDATE
        bookSoldRecordMapper.updateSettlementId(settlementVO.getId(), saleRecordIds);

        // 상계 내역 INSERT + 임대료 UPDATE
        for (DeductionEntry entry : deductions) {
            SettlementRentalOffsetVO offsetVO = new SettlementRentalOffsetVO();
            offsetVO.setSettlementId(settlementVO.getId());
            offsetVO.setRentalSettlementId(entry.rentalId());
            offsetVO.setOffsetAmount(entry.offsetAmount());
            settlementRentalOffsetMapper.insert(offsetVO);

            rentalSettlementMapper.updateDeducted(
                    entry.rentalId(), entry.deductedAmount(), entry.remainingAmount(), entry.status()
            );
        }

        return settlementVO;
    }

    @Override
    @Transactional
    public List<SettlementVO> settleAll() {
        List<Long> ownerIds = bookSoldRecordMapper.selectUnsettledBookOwnerIds();
        List<SettlementVO> results = new ArrayList<>();

        for (Long ownerId : ownerIds) {
            List<BookSoldRecordVO> unsettled = bookSoldRecordMapper.selectUnsettledByBookOwnerId(ownerId);
            if (unsettled.isEmpty()) continue;

            List<Long> saleRecordIds = unsettled.stream().map(BookSoldRecordVO::getId).toList();
            SettlementRequestDto dto = new SettlementRequestDto();
            dto.setBookOwnerId(ownerId);
            dto.setSaleRecordIds(saleRecordIds);

            try {
                SettlementVO result = settle(dto);
                results.add(result);
            } catch (ApplicationException e) {
                log.warn("배치 정산 실패 - bookOwnerId: {}, reason: {}", ownerId, e.getMessage());
            }
        }

        return results;
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
                                          int storeAmount, String payoutKey, String transferStatus,
                                          int deductedRentalAmount, int actualPayoutAmount) {
        SettlementVO settlementVO = new SettlementVO();
        settlementVO.setBookOwnerId(bookOwnerId);
        settlementVO.setTotalAmount(totalAmount);
        settlementVO.setOwnerAmount(ownerAmount);
        settlementVO.setStoreAmount(storeAmount);
        settlementVO.setPayoutKey(payoutKey);
        settlementVO.setTransferStatus(transferStatus);
        settlementVO.setDeductedRentalAmount(deductedRentalAmount);
        settlementVO.setActualPayoutAmount(actualPayoutAmount);

        int insertResult = settlementMapper.insert(settlementVO);
        if (insertResult != 1) {
            throw new ApplicationException(ErrorCode.SETTLEMENT_INSERT_FAIL);
        }

        return settlementMapper.selectById(settlementVO.getId());
    }

    private record DeductionEntry(Long rentalId, int deductedAmount, int remainingAmount,
                                   String status, int offsetAmount) {}
}

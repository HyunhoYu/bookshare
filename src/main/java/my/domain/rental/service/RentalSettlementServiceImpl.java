package my.domain.rental.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.rental.RentalSettlementDetailVO;
import my.domain.rental.RentalSettlementMapper;
import my.domain.rental.RentalSettlementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalSettlementServiceImpl implements RentalSettlementService {

    private final RentalSettlementMapper rentalSettlementMapper;

    @Override
    public void generateSettlements(Long occupiedRecordId, Long bookOwnerId,
                                    LocalDate startDate, LocalDate expirationDate, int monthlyPrice) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(expirationDate);

        for (YearMonth current = startMonth; !current.isAfter(endMonth); current = current.plusMonths(1)) {
            int amount;
            if (current.equals(startMonth)) {
                int totalDays = current.lengthOfMonth();
                int remainingDays = totalDays - (startDate.getDayOfMonth() - 1);
                amount = (monthlyPrice * remainingDays) / totalDays;
            } else {
                amount = monthlyPrice;
            }

            RentalSettlementVO settlement = new RentalSettlementVO();
            settlement.setOccupiedRecordId(occupiedRecordId);
            settlement.setBookOwnerId(bookOwnerId);
            settlement.setTargetMonth(current.format(formatter));
            settlement.setAmount(amount);

            int result = rentalSettlementMapper.insert(settlement);
            if (result != 1) {
                throw new ApplicationException(ErrorCode.RENTAL_SETTLEMENT_INSERT_FAIL);
            }
        }
    }

    @Override
    public List<RentalSettlementDetailVO> findAll() {
        return rentalSettlementMapper.selectAllDetail();
    }

    @Override
    public List<RentalSettlementDetailVO> findByBookOwnerId(Long bookOwnerId) {
        return rentalSettlementMapper.selectDetailByBookOwnerId(bookOwnerId);
    }

    @Override
    @Transactional
    public RentalSettlementVO pay(Long id) {
        RentalSettlementVO settlement = requireNonNull(
                rentalSettlementMapper.selectById(id),
                ErrorCode.RENTAL_SETTLEMENT_NOT_FOUND);

        if ("PAID".equals(settlement.getStatus())) {
            throw new ApplicationException(ErrorCode.RENTAL_SETTLEMENT_ALREADY_PAID);
        }

        int result = rentalSettlementMapper.updateStatusPaid(id);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.RENTAL_SETTLEMENT_ALREADY_PAID);
        }

        return rentalSettlementMapper.selectById(id);
    }
}

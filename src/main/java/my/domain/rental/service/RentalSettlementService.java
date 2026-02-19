package my.domain.rental.service;

import my.domain.rental.RentalSettlementDetailVO;
import my.domain.rental.RentalSettlementVO;

import java.time.LocalDate;
import java.util.List;

public interface RentalSettlementService {

    void generateSettlements(Long occupiedRecordId, Long bookOwnerId,
                             LocalDate startDate, LocalDate expirationDate, int monthlyPrice);
    List<RentalSettlementDetailVO> findAll();
    List<RentalSettlementDetailVO> findByBookOwnerId(Long bookOwnerId);
    RentalSettlementVO pay(Long id);
}

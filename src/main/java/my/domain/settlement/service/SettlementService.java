package my.domain.settlement.service;

import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.settlement.dto.SettlementRequestDto;
import my.domain.settlement.vo.SettlementVO;

import java.util.List;

public interface SettlementService {
    List<SettlementVO> findAll(Long BookOwnerId);
    List<SettlementVO> findSettled(Long BookOwnerId);
    List<BookSoldRecordVO> findUnSettled(Long BookOwnerId);
    List<BookSoldRecordVO> findAllUnsettled();
    SettlementVO settle(SettlementRequestDto requestDto);
}

package my.domain.settlement.service;

import my.domain.settlement.vo.SettlementVO;

import java.util.List;

public interface SettlementService {
    List<SettlementVO> findAll(Long BookOwnerId);
    List<SettlementVO> findSettled(Long BookOwnerId);
    List<SettlementVO> findUnSettled(Long BookOwnerId);
}

package my.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import my.domain.settlement.SettlementMapper;
import my.domain.settlement.vo.SettlementVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService{

    private final SettlementMapper settlementMapper;

    @Override
    public List<SettlementVO> findAll(Long bookOwnerId) {
        return settlementMapper.selectAllByBookOwnerId(bookOwnerId);
    }

    @Override
    public List<SettlementVO> findSettled(Long bookOwnerId) {
        return settlementMapper.selectSettledByBookOwnerId(bookOwnerId);
    }

    @Override
    public List<SettlementVO> findUnSettled(Long bookOwnerId) {
        return settlementMapper.selectUnSettledByBookOwnerId(bookOwnerId);
    }
}

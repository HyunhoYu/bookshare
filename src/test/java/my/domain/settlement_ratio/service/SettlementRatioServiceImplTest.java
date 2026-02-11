package my.domain.settlement_ratio.service;

import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SettlementRatioServiceImplTest {

    @Autowired
    private SettlementRatioService settlementRatioService;

    private SettlementRatioVO createVO(double ownerRatio, double adminRatio) {
        SettlementRatioVO vo = new SettlementRatioVO();
        vo.setOwnerRatio(ownerRatio);
        vo.setStoreRatio(adminRatio);
        return vo;
    }

    @Test
    @DisplayName("정산 비율 저장 성공 - ID 반환")
    void setRatio_success() {
        // given
        SettlementRatioVO vo = createVO(0.7, 0.3);

        // when
        long id = settlementRatioService.create(vo);

        // then
        assertThat(id).isGreaterThan(0);
    }

    @Test
    @DisplayName("정산 비율 저장 후 조회 - 최신 비율 일치")
    void setRatio_thenGetRatio() {
        // given
        SettlementRatioVO vo = createVO(0.6, 0.4);

        // when
        settlementRatioService.create(vo);
        SettlementRatioVO current = settlementRatioService.findCurrentRatio();

        // then
        assertThat(current).isNotNull();
        assertThat(current.getOwnerRatio()).isEqualTo(0.6);
        assertThat(current.getStoreRatio()).isEqualTo(0.4);
    }

    @Test
    @DisplayName("비율 두 번 저장 시 최신 것 조회")
    void setRatioTwice_getLatest() {
        // given
        settlementRatioService.create(createVO(0.5, 0.5));
        settlementRatioService.create(createVO(0.8, 0.2));

        // when
        SettlementRatioVO current = settlementRatioService.findCurrentRatio();

        // then
        assertThat(current.getOwnerRatio()).isEqualTo(0.8);
        assertThat(current.getStoreRatio()).isEqualTo(0.2);
    }
}

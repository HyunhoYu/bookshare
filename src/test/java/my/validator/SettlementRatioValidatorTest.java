package my.validator;

import jakarta.validation.ConstraintValidatorContext;
import my.domain.settlement_ratio.vo.SettlementRatioVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettlementRatioValidatorTest {

    private SettlementRatioValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new SettlementRatioValidator();
        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    private SettlementRatioVO createVO(double ownerRatio, double adminRatio) {
        SettlementRatioVO vo = new SettlementRatioVO();
        vo.setOwnerRatio(ownerRatio);
        vo.setStoreRatio(adminRatio);
        return vo;
    }

    @Test
    @DisplayName("정상 비율 (0.7 + 0.3) - 통과")
    void validRatio_07_03() {
        assertThat(validator.isValid(createVO(0.7, 0.3), context)).isTrue();
    }

    @Test
    @DisplayName("정상 비율 (0.5 + 0.5) - 통과")
    void validRatio_05_05() {
        assertThat(validator.isValid(createVO(0.5, 0.5), context)).isTrue();
    }

    @Test
    @DisplayName("정상 비율 (1.0 + 0.0) - 통과")
    void validRatio_10_00() {
        assertThat(validator.isValid(createVO(1.0, 0.0), context)).isTrue();
    }

    @Test
    @DisplayName("정상 비율 (0.25 + 0.75) - 통과")
    void validRatio_025_075() {
        assertThat(validator.isValid(createVO(0.25, 0.75), context)).isTrue();
    }

    @Test
    @DisplayName("합이 1이 아닌 경우 - 실패")
    void invalidRatio_sumNotOne() {
        assertThat(validator.isValid(createVO(0.5, 0.3), context)).isFalse();
    }

    @Test
    @DisplayName("소수점 3자리 (ownerRatio) - 실패")
    void invalidRatio_ownerExceedsDecimalLimit() {
        assertThat(validator.isValid(createVO(0.123, 0.877), context)).isFalse();
    }

    @Test
    @DisplayName("소수점 3자리 (adminRatio) - 실패")
    void invalidRatio_adminExceedsDecimalLimit() {
        assertThat(validator.isValid(createVO(0.87, 0.131), context)).isFalse();
    }

    @Test
    @DisplayName("둘 다 소수점 3자리 - 실패")
    void invalidRatio_bothExceedDecimalLimit() {
        assertThat(validator.isValid(createVO(0.333, 0.667), context)).isFalse();
    }
}

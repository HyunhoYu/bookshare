package my.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import my.annotation.ValidSettlementRatio;
import my.domain.settlement_ratio.dto.SettlementRatioRequestDto;
import my.domain.settlement_ratio.vo.SettlementRatioVO;

import java.math.BigDecimal;

public class SettlementRatioValidator implements ConstraintValidator<ValidSettlementRatio, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        double ownerRatio;
        double storeRatio;

        if (value instanceof SettlementRatioVO vo) {
            ownerRatio = vo.getOwnerRatio();
            storeRatio = vo.getStoreRatio();
        } else if (value instanceof SettlementRatioRequestDto dto) {
            ownerRatio = dto.getOwnerRatio();
            storeRatio = dto.getStoreRatio();
        } else {
            return false;
        }

        if (exceedsDecimalPlaceLimit(ownerRatio) || exceedsDecimalPlaceLimit(storeRatio)) {
            setMessage(context, "소수점 둘째자리까지만 입력 가능합니다");
            return false;
        }

        if (!ratioSumEqualsOne(ownerRatio, storeRatio)) {
            setMessage(context, "두 비율의 합이 1이어야 합니다");
            return false;
        }

        return true;
    }

    private boolean exceedsDecimalPlaceLimit(double value) {
        return getDecimalPlaces(value) > 2;
    }

    private boolean ratioSumEqualsOne(double ownerRatio, double storeRatio) {
        BigDecimal owner = BigDecimal.valueOf(ownerRatio);
        BigDecimal store = BigDecimal.valueOf(storeRatio);
        return owner.add(store).compareTo(BigDecimal.ONE) == 0;
    }

    private void setMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }

    private int getDecimalPlaces(double value) {
        String text = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        int index = text.indexOf(".");
        return index < 0 ? 0 : text.length() - index - 1;
    }
}

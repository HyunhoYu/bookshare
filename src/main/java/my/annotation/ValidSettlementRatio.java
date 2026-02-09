package my.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import my.validator.SettlementRatioValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SettlementRatioValidator.class)
public @interface ValidSettlementRatio {
    String message() default "비율의 합이 1이어야 하며, 소수점 둘째자리까지만 허용됩니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

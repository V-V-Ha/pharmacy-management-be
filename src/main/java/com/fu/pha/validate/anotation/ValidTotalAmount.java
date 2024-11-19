package com.fu.pha.validate.anotation;

import com.fu.pha.exception.Message;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TotalAmountValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTotalAmount {
    String message() default Message.TOTAL_AMOUNT_VALID;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

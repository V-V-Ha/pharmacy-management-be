package com.fu.pha.validate.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReturnOrderRequestDtoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidReturnOrder {
    String message() default "Invalid return order quantities";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


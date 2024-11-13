package com.fu.pha.validate.anotation;

import com.fu.pha.exception.Message;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TaxValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTax {
    String message() default Message.INVALID_TAX;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

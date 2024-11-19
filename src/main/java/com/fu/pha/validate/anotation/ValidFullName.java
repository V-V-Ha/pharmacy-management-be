package com.fu.pha.validate.anotation;

import com.fu.pha.exception.Message;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FullNameValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFullName {
    String message() default Message.INVALID_NAME;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

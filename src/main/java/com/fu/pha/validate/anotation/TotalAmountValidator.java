package com.fu.pha.validate.anotation;

import com.fu.pha.exception.Message;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TotalAmountValidator implements ConstraintValidator<ValidTotalAmount, Double> {

    @Override
    public void initialize(ValidTotalAmount constraintAnnotation) {
        // Không cần khởi tạo
    }

    @Override
    public boolean isValid(Double totalAmount, ConstraintValidatorContext context) {
        // Kiểm tra nếu trường totalAmount là null
        if (totalAmount == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.TOTAL_AMOUNT_NOT_NULL).addConstraintViolation();
            return false;
        }

        // Kiểm tra nếu totalAmount >= 0.0
        if (totalAmount < 0.0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.TOTAL_AMOUNT_VALID).addConstraintViolation();
            return false;
        }

        return true;
    }
}

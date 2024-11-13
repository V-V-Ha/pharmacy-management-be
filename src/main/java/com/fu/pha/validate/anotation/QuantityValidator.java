package com.fu.pha.validate.anotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuantityValidator implements ConstraintValidator<ValidQuantity, Integer> {
    @Override
    public void initialize(ValidQuantity constraintAnnotation) {
        // Không cần khởi tạo
    }

    @Override
    public boolean isValid(Integer quantity, ConstraintValidatorContext context) {
        // Kiểm tra nếu trường quantity là null
        if (quantity == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Số lượng không được để trống").addConstraintViolation();
            return false;
        }

        // Kiểm tra nếu quantity >= 1
        if (quantity < 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Số lượng phải lớn hơn 0").addConstraintViolation();
            return false;
        }

        return true;
    }
}

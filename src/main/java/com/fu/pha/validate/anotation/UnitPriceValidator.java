package com.fu.pha.validate.anotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UnitPriceValidator implements ConstraintValidator<ValidUnitPrice, Double> {
    @Override
    public void initialize(ValidUnitPrice constraintAnnotation) {
        // Không cần khởi tạo
    }

    @Override
    public boolean isValid(Double unitPrice, ConstraintValidatorContext context) {
        // Kiểm tra nếu trường unitPrice là null
        if (unitPrice == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Đơn giá không được để trống").addConstraintViolation();
            return false;
        }

        // Kiểm tra nếu unitPrice >= 0.01
        if (unitPrice < 0.01) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Đơn giá phải lớn hơn 0").addConstraintViolation();
            return false;
        }

        return true;
    }
}

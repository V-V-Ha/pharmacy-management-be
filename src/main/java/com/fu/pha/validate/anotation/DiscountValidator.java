package com.fu.pha.validate.anotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DiscountValidator implements ConstraintValidator<ValidDiscount, Double> {

    @Override
    public void initialize(ValidDiscount constraintAnnotation) {
        // Không cần khởi tạo
    }

    @Override
    public boolean isValid(Double discount, ConstraintValidatorContext context) {
        // Kiểm tra nếu discount là null (cho phép null nếu trường hợp này hợp lệ)
        if (discount == null) {
            discount = 0.0;
        }

        // Kiểm tra nếu discount >= 0
        if (discount < 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Chiết khấu phải lớn hơn hoặc bằng 0").addConstraintViolation();
            return false;
        }

        // Kiểm tra nếu discount <= 100
        if (discount > 100) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Chiết khấu phải nhỏ hơn hoặc bằng 100").addConstraintViolation();
            return false;
        }

        return true;
    }
}

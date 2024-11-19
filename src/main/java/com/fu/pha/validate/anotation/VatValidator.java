package com.fu.pha.validate.anotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VatValidator implements ConstraintValidator<ValidVat, Double> {

    @Override
    public void initialize(ValidVat constraintAnnotation) {
        // Không cần khởi tạo
    }

    @Override
    public boolean isValid(Double tax, ConstraintValidatorContext context) {
        // Kiểm tra nếu tax là null (cho phép null nếu trường hợp này hợp lệ)
        if (tax == null) {
            return true;  // Nếu thuế không bắt buộc, có thể trả về true ở đây.
        }

        // Kiểm tra nếu tax >= 0
        if (tax < 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Thuế phải lớn hơn hoặc bằng 0").addConstraintViolation();
            return false;
        }

        // Kiểm tra nếu tax <= 100
        if (tax > 100) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Thuế phải nhỏ hơn hoặc bằng 100").addConstraintViolation();
            return false;
        }

        return true;
    }
}

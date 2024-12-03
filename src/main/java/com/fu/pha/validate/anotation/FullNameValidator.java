package com.fu.pha.validate.anotation;

import com.cloudinary.utils.StringUtils;
import com.fu.pha.exception.Message;
import com.fu.pha.validate.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FullNameValidator implements ConstraintValidator<ValidFullName, String> {
    @Override
    public void initialize(ValidFullName constraintAnnotation) {
        // Không cần khởi tạo
    }

    @Override
    public boolean isValid(String fullName, ConstraintValidatorContext context) {
        // Kiểm tra nếu trường fullName không trống
        if (StringUtils.isBlank(fullName)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.NULL_NAME).addConstraintViolation();
            return false;
        }

        // Kiểm tra nếu fullName khớp với biểu thức chính quy
        if (!fullName.matches(Constants.REGEX_NAME)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.INVALID_NAME).addConstraintViolation();
            return false;
        }

        return true;
    }
}

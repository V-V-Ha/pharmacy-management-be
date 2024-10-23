package com.fu.pha.util;

import com.fu.pha.validate.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalEmailValidator implements ConstraintValidator<OptionalEmail, String> {

    private static final String EMAIL_REGEX = Constants.REGEX_GMAIL;

    @Override
    public void initialize(OptionalEmail constraintAnnotation) {}

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true; // Bỏ qua nếu email null hoặc rỗng
        }
        return email.matches(EMAIL_REGEX); // Kiểm tra định dạng email nếu có giá trị
    }
}


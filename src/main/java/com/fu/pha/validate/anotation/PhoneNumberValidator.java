package com.fu.pha.validate.anotation;

import com.cloudinary.utils.StringUtils;
import com.fu.pha.exception.Message;
import com.fu.pha.validate.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        // Check if the phone number is not blank
        if (StringUtils.isBlank(phoneNumber)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.NULL_FILED).addConstraintViolation();
            return false;
        }

        // Check if the phone number matches the pattern
        if (!phoneNumber.matches(Constants.REGEX_PHONE)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.INVALID_PHONE).addConstraintViolation();
            return false;
        }

        return true;
    }
}

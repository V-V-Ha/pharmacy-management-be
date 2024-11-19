package com.fu.pha.validate.anotation;

import com.cloudinary.utils.StringUtils;
import com.fu.pha.exception.Message;
import com.fu.pha.validate.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaxValidator implements ConstraintValidator<ValidTax, String> {

    @Override
    public void initialize(ValidTax constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String tax, ConstraintValidatorContext context) {
        // Check if the tax is not blank
        if (StringUtils.isBlank(tax)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.NULL_FILED).addConstraintViolation();
            return false;
        }

        // Check if the tax matches the pattern
        if (!tax.matches(Constants.REGEX_TAX)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(Message.INVALID_TAX).addConstraintViolation();
            return false;
        }

        return true;
    }
}

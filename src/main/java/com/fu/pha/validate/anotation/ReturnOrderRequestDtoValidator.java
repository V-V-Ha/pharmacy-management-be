package com.fu.pha.validate.anotation;

import com.fu.pha.dto.request.ReturnOrderItemRequestDto;
import com.fu.pha.dto.request.ReturnOrderRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ReturnOrderRequestDtoValidator implements ConstraintValidator<ValidReturnOrder, ReturnOrderRequestDto> {

    @Override
    public void initialize(ValidReturnOrder constraintAnnotation) {
    }

    @Override
    public boolean isValid(ReturnOrderRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getReturnOrderItems() == null || dto.getReturnOrderItems().isEmpty()) {
            return false;
        }

        List<ReturnOrderItemRequestDto> items = dto.getReturnOrderItems();
        if (items.size() == 1) {
            ReturnOrderItemRequestDto item = items.get(0);
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Số lượng phải lớn hơn 0 ")
                        .addPropertyNode("returnOrderItems")
                        .addConstraintViolation();
                return false;
            }
        } else {
            boolean atLeastOneValid = items.stream()
                    .anyMatch(item -> item.getQuantity() != null && item.getQuantity() > 0);
            if (!atLeastOneValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Phải có ít nhất một sản phẩm có số lượng lớn hơn 0")
                        .addPropertyNode("returnOrderItems")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
package com.fu.pha.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Tiền mặt"),
    TRANSFER("Chuyển khoản");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

}

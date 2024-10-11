package com.fu.pha.enums;

import lombok.Data;
import lombok.Getter;

@Getter

public enum PaymentStatus {

    PAID("Đã thanh toán"),
    UNPAID("Chưa thanh toán");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }
}

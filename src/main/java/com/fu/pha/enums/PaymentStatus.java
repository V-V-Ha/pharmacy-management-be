package com.fu.pha.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    PAID("Đã thanh toán"),
    UNPAID("Chưa thanh toán");

    PaymentStatus(String paid) {
    }
}

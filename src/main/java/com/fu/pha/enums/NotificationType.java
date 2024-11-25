package com.fu.pha.enums;

public enum NotificationType {
    STOCK_IN_OUT("Thông báo về nhập xuất"),
    EXPIRED("Thông báo về hết hạn"),
    OUT_OF_STOCK("Thông báo về hết hàng");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

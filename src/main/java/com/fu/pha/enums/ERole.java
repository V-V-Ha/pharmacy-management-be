package com.fu.pha.enums;

import lombok.Getter;

@Getter
public enum ERole {
    ROLE_PRODUCT_OWNER("Chủ cửa hàng"),
    ROLE_SALE("Nhân viên bán hàng"),
    ROLE_STOCK("Nhân viên kho"),;

    ERole(String s) {
    }
}

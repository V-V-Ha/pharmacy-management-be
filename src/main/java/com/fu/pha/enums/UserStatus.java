package com.fu.pha.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum UserStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Ngưng hoạt động");

    UserStatus(String inactive) {
    }
}

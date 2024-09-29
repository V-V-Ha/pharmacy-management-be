package com.fu.pha.enums;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("Hoạt động"),
    INACTIVE("Ngưng hoạt động");

    Status(String inactive) {
    }
}

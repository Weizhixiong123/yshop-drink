package com.clubhub.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    OWNER("owner", "店东"),
    STAFF("staff", "店员"),
    CUSTOMER("customer", "客户");

    private final String code;
    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}

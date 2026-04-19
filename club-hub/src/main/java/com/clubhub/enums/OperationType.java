package com.clubhub.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    ADD_POINTS("add_points", "加积分"),
    SUB_POINTS("sub_points", "减积分"),
    ADD_WINE("add_wine", "加酒"),
    SUB_WINE("sub_wine", "减酒"),
    ADD_BALANCE("add_balance", "加储值"),
    SUB_BALANCE("sub_balance", "减储值");

    private final String code;
    private final String name;

    OperationType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String nameOf(String code) {
        if (code == null) return "";
        for (OperationType t : values()) {
            if (t.code.equals(code)) return t.name;
        }
        return code;
    }
}

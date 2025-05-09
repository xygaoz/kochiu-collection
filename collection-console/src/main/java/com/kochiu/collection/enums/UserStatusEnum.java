package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    NORMAL(1, "enable", "正常"),
    STOP(2, "disable", "停用")
    ;

    private final int code;
    private final String message;
    private final String name;

    UserStatusEnum(int code, String name, String message) {
        this.code = code;
        this.message = message;
        this.name = name;
    }

    public static UserStatusEnum getByCode(int code) {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            if (statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }

    public static UserStatusEnum getByName(String name) {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            if (statusEnum.getName().equals(name)) {
                return statusEnum;
            }
        }
        return null;
    }
}

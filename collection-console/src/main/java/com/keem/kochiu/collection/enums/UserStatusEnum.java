package com.keem.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    NORMAL(1, "正常"),
    STOP(2, "停用")
    ;

    private final int code;
    private final String message;

    UserStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static UserStatusEnum getByCode(int code) {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            if (statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }
}

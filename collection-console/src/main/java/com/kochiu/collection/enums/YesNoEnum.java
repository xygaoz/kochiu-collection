package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum YesNoEnum {
    YES(1, "是"),
    NO(0, "否");

    private final int code;
    private final String message;

    YesNoEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static YesNoEnum getEnum(int code) {
        for (YesNoEnum yesNoEnum : values()) {
            if (yesNoEnum.getCode() == code) {
                return yesNoEnum;
            }
        }
        return null;
    }
}

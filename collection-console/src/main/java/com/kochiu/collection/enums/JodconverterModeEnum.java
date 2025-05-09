package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum JodconverterModeEnum {

    LOCAL("local"),
    REMOTE("remote");

    private final String mode;

    JodconverterModeEnum(String mode) {
        this.mode = mode;
    }

    public static JodconverterModeEnum getByMode(String mode) {
        for (JodconverterModeEnum jodconverterModeEnum : JodconverterModeEnum.values()) {
            if (jodconverterModeEnum.getMode().equals(mode)) {
                return jodconverterModeEnum;
            }
        }
        return null;
    }
}

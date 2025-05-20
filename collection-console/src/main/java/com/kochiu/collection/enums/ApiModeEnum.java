package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum ApiModeEnum {

    LOCAL("local"),
    REMOTE("remote");

    private final String mode;

    ApiModeEnum(String mode) {
        this.mode = mode;
    }

    public static ApiModeEnum getByMode(String mode) {
        for (ApiModeEnum jodconverterModeEnum : ApiModeEnum.values()) {
            if (jodconverterModeEnum.getMode().equals(mode)) {
                return jodconverterModeEnum;
            }
        }
        return null;
    }
}

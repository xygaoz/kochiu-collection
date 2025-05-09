package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum SaveTypeEnum {

    LOCAL(1, "系统本地存储"),
    LINK(2, "本地链接"),
    NETWORK(3, "网络"),
    ;

    private final int code;

    private final String desc;

    SaveTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SaveTypeEnum getByCode(int code) {
        for (SaveTypeEnum saveTypeEnum : values()) {
            if (saveTypeEnum.getCode() == code) {
                return saveTypeEnum;
            }
        }
        return null;
    }
}

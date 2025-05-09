package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum CategoryByEnum {

    CREATE_TIME_ABS(1, "创建时间顺序"),
    CREATE_TIME_DESC(2, "创建时间倒序"),
    RESOURCE_NUM_DESC(3, "资源数量倒叙");

    private final int code;
    private final String desc;

    CategoryByEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CategoryByEnum getByCode(int code) {
        for (CategoryByEnum categoryByEnum : CategoryByEnum.values()) {
            if (categoryByEnum.code == code) {
                return categoryByEnum;
            }
        }
        return null;
    }
}

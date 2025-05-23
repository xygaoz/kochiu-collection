package com.kochiu.collection.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TagByEnum {

    CREATE_TIME_ABS(1, "创建时间顺序"),
    CREATE_TIME_DESC(2, "创建时间倒序"),
    RESOURCE_NUM_DESC(3, "资源数量倒叙");

    @JsonValue
    private final int code;
    private final String desc;

    TagByEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TagByEnum getByCode(int code) {
        for (TagByEnum categoryByEnum : TagByEnum.values()) {
            if (categoryByEnum.code == code) {
                return categoryByEnum;
            }
        }
        return CREATE_TIME_ABS;
    }

    public String toString() {
        return String.valueOf(code);
    }
}

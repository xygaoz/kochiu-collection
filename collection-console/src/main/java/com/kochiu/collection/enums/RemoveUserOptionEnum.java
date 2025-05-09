package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum RemoveUserOptionEnum {

    DELETE_RESOURCE("delete", "删除资源"),
    KEEP_RESOURCE("keep", "保留资源及目录");

    private String value;
    private String desc;

    RemoveUserOptionEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static RemoveUserOptionEnum getByValue(String value) {
        for (RemoveUserOptionEnum item : RemoveUserOptionEnum.values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}

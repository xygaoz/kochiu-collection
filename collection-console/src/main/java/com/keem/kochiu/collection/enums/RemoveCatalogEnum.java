package com.keem.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum RemoveCatalogEnum {
    REMOVE_TYPE_DELETE(2, "删除"),
    REMOVE_TYPE_MOVE(1, "移动"),
    ;

    private int value;
    private String desc;

    RemoveCatalogEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static RemoveCatalogEnum getEnum(int value) {
        for (RemoveCatalogEnum e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return null;
    }
}

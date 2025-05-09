package com.kochiu.collection.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RemoveEnum {
    REMOVE_TYPE_DELETE(2, "删除"),
    REMOVE_TYPE_MOVE(1, "移动");

    private final int code;
    private final String desc;

    RemoveEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 用 @JsonValue 指定序列化字段
    @JsonValue
    public int getCode() {
        return this.code;
    }

    // 反序列化方法（兼容 String 和 Integer 输入）
    public static RemoveEnum fromCode(Object code) {
        if (code == null) {
            return null;
        }
        int codeValue;
        if (code instanceof Integer) {
            codeValue = (Integer) code;
        } else {
            try {
                codeValue = Integer.parseInt(code.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid RemoveEnum code: " + code);
            }
        }
        for (RemoveEnum e : values()) {
            if (e.code == codeValue) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant for code: " + code);
    }
}
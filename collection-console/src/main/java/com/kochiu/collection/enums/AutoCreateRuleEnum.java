package com.kochiu.collection.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 自动创建目录规则枚举
 */
@Getter
public enum AutoCreateRuleEnum {

    /** 在根目录创建日期目录 (格式: YYYY-MM-DD) */
    CREATE_DATE_DIRECTORY(1, "在根目录创建日期目录 (格式: YYYY-MM-DD)"),

    /** 在根目录按服务端路径子目录结构创建 */
    MIRROR_SOURCE_DIRECTORY(2, "在根目录按服务端路径子目录结构创建"),

    /** 不自动创建 (仅导入到根目录) */
    NO_AUTO_CREATE(3, "不自动创建 (仅导入到根目录)");

    private final int code;
    private final String description;

    AutoCreateRuleEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    /**
     * 根据 code 获取枚举
     */
    public static AutoCreateRuleEnum getByCode(Object code) {
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
        for (AutoCreateRuleEnum e : values()) {
            if (e.code == codeValue) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant for code: " + code);
    }

    /**
     * 检查是否允许自动创建目录
     */
    public boolean isAutoCreateAllowed() {
        return this != NO_AUTO_CREATE;
    }
}
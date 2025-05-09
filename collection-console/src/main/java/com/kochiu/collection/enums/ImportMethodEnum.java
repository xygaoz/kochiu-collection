package com.kochiu.collection.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 资源导入方式枚举
 */
@Getter
public enum ImportMethodEnum {
    /**
     * 复制到新位置
     */
    COPY(1, "复制到新位置"),

    /**
     * 移动到新位置
     */
    MOVE(2, "移动到新位置"),

    /**
     * 保持原路径 (仅建立索引)
     */
    KEEP_ORIGINAL(3, "保持原路径 (仅建立索引)");

    private final int code;
    private final String description;

    ImportMethodEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    /**
     * 根据code获取枚举
     * @param code 枚举code
     * @return 对应的枚举，找不到返回null
     */
    public static ImportMethodEnum getByCode(Object code) {
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
        for (ImportMethodEnum e : values()) {
            if (e.code == codeValue) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant for code: " + code);
    }

    /**
     * 检查code是否有效
     * @param code 要检查的code
     * @return 如果有效返回true，否则返回false
     */
    public static boolean isValid(int code) {
        return getByCode(code) != null;
    }
}
package com.keem.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

    SUCCESS("0000", "成功"),

    SYS_ERROR("1000", "系统异常"),
    ERROR_PARAM("1001", "参数错误"),
    ERROR_TOKEN_EXPIRE("1002", "token过期"),
    ERROR_TOKEN_INVALID("1003", "token无效"),
    ERROR_TOKEN_NOT_EXIST("1004", "token不存在"),
    ;

    private final String code;
    private final String message;
    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

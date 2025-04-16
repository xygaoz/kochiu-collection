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
    CATEGORY_NOT_EXIST("1005", "分类不存在"),
    ILLEGAL_REQUEST("1006", "非法请求"),
    INVALID_USERNAME_OR_PASSWORD("1007", "无效的用户名或密码"),
    FAILED_GET_PUBLIC_KEY("1008", "获取公钥失败"),
    FAILED_GET_PRIVATE_KEY("1009", "获取私钥失败"),
    FAILED_GET_COMMON_KEY("1010", "获取公用加密key失败"),
    FAILED_GET_DEFAULT_CATEGORY("1011", "获取默认分类失败"),
    UNSUPPORTED_FILE_TYPES("1012", "不支持的文件类型"),
    FILE_SAVING_FAILURE("1013", "文件保存失败"),
    FILE_IS_EXIST("1014", "文件已存在"),
    CONTENT_CANNOT_BE_EMPTY("1015", "更新内容不能为空"),
    CATEGORY_IS_EXIST("1016", "分类已存在"),
    TAG_IS_EXIST("1017", "标签已存在"),
    FAILED_GEN_TOKEN("1018", "生成token失败"),
    ROOT_FOLDER_IS_INVALID("1019", "根目录无效"),
    ;

    private final String code;
    private final String message;
    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

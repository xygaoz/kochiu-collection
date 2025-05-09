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
    TARGET_CATALOG_IS_INVALID("1020", "目标目录无效"),
    ADD_CATALOG_FAIL("1029", "新建目录失败"),
    CATALOG_NOT_EXIST("1030", "目录不存在"),
    CATALOG_NAME_IS_SAME("1031", "目录名称相同"),
    UPDATE_CATALOG_FAIL("1032", "目录改名/移动失败"),
    CATALOG_IS_EXIST("1033", "目录已经存在"),
    CATALOG_LEVEL_IS_MAX("1034", "目录层次已经达到最大"),
    REMOVE_CATALOG_FAIL("1035", "目录删除失败"),
    CATALOG_CREATE_FAILURE("1036", "创建目录失败"),
    TARGET_CATALOG_IS_SAME("1037", "目标目录相同"),
    RESOURCE_NOT_EXIST("1038", "资源文件不存在"),
    TARGET_CATALOG_IS_NOT_NULL("1039", "目标目录ID不能为空"),
    SERVER_PATH_ERROR("1040", "服务端资源目录不存在或不能访问"),
    TASK_CANCELLED("1041", "任务取消"),
    IMPORT_ERROR("1042", "导入错误"),
    IMPORT_METHOD_ERROR("1043", "导入方式错误"),
    AUTO_CREATE_RULE_ERROR("1044", "创建目录规则错误"),
    ROOT_CATALOG_IS_INVALID("1045", "根目录信息错误"),
    USER_CODE_IS_EXIST("1046", "用户编码已经存在"),
    USER_IS_NOT_EXIST("1047", "用户不存在"),
    ERROR_USER_STOP("1048", "用户已经停用"),
    USER_PASSWORD_DECRYPT_ERROR("1049", "密码解密失败"),
    PASSWORDS_ARE_INCONSISTENT_ERROR("1050", "两次输入的密码不一致"),
    USER_STATUS_ERROR("1051", "用户状态错误"),
    PERMISSION_IS_INVALID("1052", "无权限访问"),
    ROLE_IS_NOT_EXIST("1053", "角色不存在"),
    STRATEGY_IS_NOT_EXIST("1054", "存储策略不存在"),
    LOCAL_STRATEGY_IS_INVALID("1055", "获取本地存储策略失败"),
    USER_CANNOT_STOP("1056", "该用户不能停用"),
    ROLE_IS_NOT_DELETE("1057", "该角色不能删除"),
    USER_IS_NOT_DELETE("1058", "该用户不能删除"),
    USER_IS_NOT_RESET_PASSWORD("1059", "该用户不能重置密码"),
    USER_OLD_PASSWORD_ERROR("1060", "旧密码错误");

    private final String code;
    private final String message;
    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

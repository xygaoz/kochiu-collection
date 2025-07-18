package com.kochiu.collection;

/**
 * @author KoChiu
 */
public final class Constant {

    /**
     * 请求头中存放用户信息的key
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_DEVICE_FINGERPRINT = "X-Device-Fingerprint";

    public static final String PUBLIC_URL = "/api/v1";

    public static final String TOKEN_PARAMS_FLAG = "params";
    public static final String TOKEN_API_FLAG = "api";
    public static final String TOKEN_TYPE_FLAG = "type";
    public static final String TOKEN_FINGERPRINT_FLAG = "fingerprint";
    public static final String TOKEN_IP_FLAG = "ip";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    public static final int ROOT_CATALOG_SNO = 1;
    public static final String CATALOG_FLAG = "c~";

    public static final String ROOT_PATH = "/";
    public static final String ROOT_PATH_WIN = "C:";

    public static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";
    public static final String RANDOM_CHARS2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static final String DB_PATH_PROPERTY = "DB_PATH";
    public static final String CONTAINER_DB_PATH = "/app/db";
    public static final String CONTAINER_BACKUP_DB_PATH = "/tmp/db";
    public static final String CONTAINER_RESOURCE_PATH = "/app/resources";

    private Constant(){

    }
}

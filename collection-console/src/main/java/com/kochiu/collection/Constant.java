package com.kochiu.collection;

/**
 * @author KoChiu
 */
public final class Constant {

    /**
     * 请求头中存放用户信息的key
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String PUBLIC_URL = "/api/v1";

    public static final String TOKEN_PARAMS_FLAG = "params";
    public static final String TOKEN_API_FLAG = "api";
    public static final String TOKEN_TYPE_FLAG = "type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    public static final int ROOT_CATALOG_SNO = 1;

    public static final String ROOT_PATH = "/";

    public static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";

    private Constant(){

    }
}

package com.kochiu.collection.util;

import java.util.Base64;

/**
 * @author KoChiu
 */
public class HexUtils {

    /**
     * 二进制byte数组转十六进制byte数组
     */
    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (byte value : b) {
            stmp = Integer.toHexString(value & 0xFF).toUpperCase();
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString();
    }

    /**
     * 十六进制byte数组转二进制byte数组
     */
    public static byte[] hex2byte(String hex)
            throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("invalid hex string");
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = Integer.valueOf(byteint).byteValue();
        }
        return b;
    }

    /**
     * 将Base64编码的RSA加密结果转换为16进制字符串
     */
    public static String base64ToHex(String base64Str) {
        if (base64Str == null || base64Str.isEmpty()) {
            throw new IllegalArgumentException("输入字符串不能为空");
        }
        // 1. Base64解码
        byte[] encryptedBytes = Base64.getDecoder().decode(base64Str);
        return byte2hex(encryptedBytes);
    }
}

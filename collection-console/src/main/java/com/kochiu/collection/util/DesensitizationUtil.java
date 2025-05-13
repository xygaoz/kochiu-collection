package com.kochiu.collection.util;

public class DesensitizationUtil {

    /**
     * 通用脱敏方法
     * @param str 原始字符串
     * @param start 开始位置(从0开始)
     * @param end 结束位置
     * @param maskChar 脱敏字符
     * @return 脱敏后的字符串
     */
    public static String mask(String str, int start, int end, char maskChar) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        if (start < 0) start = 0;
        if (end > str.length()) end = str.length();
        if (start >= end) return str;

        char[] chars = str.toCharArray();
        for (int i = start; i < end; i++) {
            chars[i] = maskChar;
        }
        return new String(chars);
    }

    // 手机号脱敏 (保留前3后4)
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return mask(phone, 3, phone.length() - 4, '*');
    }

    // 身份证号脱敏 (保留前1后1)
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 3) {
            return idCard;
        }
        return mask(idCard, 1, idCard.length() - 1, '*');
    }

    // 银行卡号脱敏 (保留前4后4)
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 9) {
            return bankCard;
        }
        return mask(bankCard, 4, bankCard.length() - 4, '*');
    }

    // 姓名脱敏 (保留姓氏)
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        return name.charAt(0) + mask(name.substring(1), 0, name.length() - 1, '*');
    }

    // 邮箱脱敏
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf("@");
        if (atIndex < 2) {
            return mask(email, 0, atIndex, '*') + email.substring(atIndex);
        }
        return mask(email, 1, atIndex, '*') + email.substring(atIndex);
    }
}
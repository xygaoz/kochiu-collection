package com.kochiu.collection.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * @author KoChiu
 */
public class AesUtil {

    /**
     * AES加密
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     * @throws Exception
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        kgen.init(128, random);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }

    public static byte[] aesEncryptToBytes(String content, byte[] encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey);
        kgen.init(128, random);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }

    /**
     * AES加密为base 64 code
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return HexUtils.byte2hex(aesEncryptToBytes(content, encryptKey)).toLowerCase();
    }

    public static String aesEncrypt(String content, byte[] encryptKey) throws Exception {
        return HexUtils.byte2hex(aesEncryptToBytes(content, encryptKey)).toLowerCase();
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey 解密密钥
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {

        KeyGenerator kgen = KeyGenerator.getInstance("AES");

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(decryptKey.getBytes());
        kgen.init(128, random);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes, StandardCharsets.UTF_8);
    }

    public static String aesDecryptByBytes(byte[] encryptBytes, byte[] decryptKey) {

        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(decryptKey);
            kgen.init(128, random);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
            byte[] decryptBytes = cipher.doFinal(encryptBytes);

            return new String(decryptBytes, StandardCharsets.UTF_8);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 将base 64 code AES解密
     * @param encryptStr 待解密的base 64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return (encryptStr == null || encryptStr.trim().isEmpty()) ? null : aesDecryptByBytes(HexUtils.hex2byte(encryptStr), decryptKey);
    }

    public static String aesDecrypt(String encryptStr, byte[] decryptKey) {
        return (encryptStr == null || encryptStr.trim().isEmpty()) ? null : aesDecryptByBytes(HexUtils.hex2byte(encryptStr), decryptKey);
    }

    public static void main(String[] args) throws Exception {
        String content = "{\"id\":\"1\",\"name\":\"test\"}";
        String key = "1234567890123456";

        String encrypt = aesEncrypt(content, key);
        System.out.println(encrypt);
        String decrypt = aesDecrypt(encrypt, key);
        System.out.println(decrypt);
    }
}

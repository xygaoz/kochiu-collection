package com.kochiu.collection.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * RSA 工具类。提供加密，解密，生成密钥对等方法。
 * @author KoChiu
 */
public class RsaHexUtil {

	/**
	 * 此对象不可以每次重复创建 会造成oom
	 * 全局都是用一个单例对象
	 */
	private static final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
	static {
		Security.addProvider(bouncyCastleProvider);
	}

	/**
	 * RSA根据modulus,exponent生成公钥
	 */
	public static PublicKey getPublicKey(String modulus, String publicExponent)

			throws NoSuchAlgorithmException, InvalidKeySpecException {

		BigInteger bigIntModulus = new BigInteger(modulus,16);
		BigInteger bigIntPrivateExponent = new BigInteger(publicExponent,16);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA",
				bouncyCastleProvider);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		return publicKey;

	}

	/**
	 * RSA公钥加密
	 */
	public static String encrypt(String str, PublicKey publicKey) throws Exception {
		// RSA加密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", bouncyCastleProvider);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
		String outStr = Base64.getEncoder().encodeToString(encryptedBytes);

		return HexUtils.byte2hex(Base64.getDecoder().decode(outStr));
	}

	/**
	 * 得到私钥对象
	 */
	public static PrivateKey getPrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = HexUtils.hex2byte(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	public static String decrypt(String str, PrivateKey privateKey) throws Exception{
		byte[] inputByte = HexUtils.hex2byte(str);
//		//RSA解密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", bouncyCastleProvider);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		printLnAlgorithm(cipher);
        return new String(cipher.doFinal(inputByte));

	}

	/**
	 * 随机生成密钥对
	 */
	public static String[] genKeyPair() throws NoSuchAlgorithmException {
		// KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		// 初始化密钥对生成器，密钥大小为96-1024位
		keyPairGen.initialize(2048,new SecureRandom());
		// 生成一个密钥对，保存在keyPair中
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥

		String[] keys = new String[2];
		String publicKeyString = HexUtils.byte2hex(publicKey.getModulus().toByteArray());
		// 得到私钥字符串
		String privateKeyString = HexUtils.byte2hex(privateKey.getEncoded());
		keys[0] = publicKeyString.substring(2).toUpperCase();
		keys[1] = privateKeyString.toUpperCase();

		return keys;
	}

	/**
	 * 打印加密参数
	 */
	private static void printLnAlgorithm(Cipher cipher){
		String algorithm=cipher.getAlgorithm();
		long blockSize=cipher.getBlockSize();
		AlgorithmParameters  algorithmParameters= cipher.getParameters();
		Provider provider= cipher.getProvider();
		System.out.println("algorithm:"+algorithm+",blockSize:"+blockSize+",Provider:"+provider.getClass().getName());
	}

    /**
     * 将PublicKey对象转换为PEM格式的字符串
     */
    public static String publicKeyToPem(PublicKey publicKey) {
        byte[] encodedPublicKey = publicKey.getEncoded();
        String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);
        return "-----BEGIN PUBLIC KEY-----\n" +
               b64PublicKey.replaceAll("(.{64})", "$1\n") +
               "-----END PUBLIC KEY-----";
    }

	public static void main(String[] args) throws Exception{
		String publicKey = "CCEE2221708EA8D0B2A862FA6EF043C4922756FD77CAB9F485F419836942FF3D51F9861802EBAB23611176CB1B7C12FC21B5E317E7A574D5608D209E06A1E3B53E203065058D1770BB4D054DC99AA94EDF768673E5548D22765C951E0C810024C19F73BC7644BC763E37815582EEEBC2FE987C47E409032058EE29B8441D98A32CD257E2B533AE3EE0A19DB270B6987798442588170E3156C637936AA9FE2575D2A04E8A1FEFFA3A851AE9ADBC028ECA27949A20BA3355CBF5148C83A7A1A8B03CA124F9620DFA0E61C001AA99C5B8C8BEB8554FBE8546AE8278B2DAFFF3C56DA6FF7240E05D65B1763B149762EB6D6E993B99229B839EA61C6094D1DD322413";
		String privateKey = "308204BD020100300D06092A864886F70D0101010500048204A7308204A30201000282010100CCEE2221708EA8D0B2A862FA6EF043C4922756FD77CAB9F485F419836942FF3D51F9861802EBAB23611176CB1B7C12FC21B5E317E7A574D5608D209E06A1E3B53E203065058D1770BB4D054DC99AA94EDF768673E5548D22765C951E0C810024C19F73BC7644BC763E37815582EEEBC2FE987C47E409032058EE29B8441D98A32CD257E2B533AE3EE0A19DB270B6987798442588170E3156C637936AA9FE2575D2A04E8A1FEFFA3A851AE9ADBC028ECA27949A20BA3355CBF5148C83A7A1A8B03CA124F9620DFA0E61C001AA99C5B8C8BEB8554FBE8546AE8278B2DAFFF3C56DA6FF7240E05D65B1763B149762EB6D6E993B99229B839EA61C6094D1DD322413020301000102820100205FF0896AE046C02DB1576DABF00E2CB4503DB86E5FDB0FCBC7BBEE1C4786CE37B09D6416B60783DFD663F783608F1940F41CABCE9F3F7D6432F777AFA18CB939D6D8FE64A99D0DE4B12EC8D2B439D229906E7E49B91950DBF51563B962F9D454B86D8ED4FA57F1B033919DA2413F82BF38F4CD60B3A1C84EF45527F4AEDE4984A54A3E5ABA1E85B260E9B61E7E075C34ACF05764A4159A5DC4CBEA3331CAA70F089F396E83C73CA7540C77833161D131A66C90E8E1AFC10C11E6B6428EEE810BC8DA7D827A9EC2FD2346AE266B7B5B98528F8861FB7FBEC321EC6C38AEEF6B2D84F9F16A080C7698F7D67F71D1427AF41CC7F3D17621F9E3A6D6AD52E3EE4502818100DD11D807066138EAF8DEB3607D8DB83A711C1685A5EE036C9D86A7981A0416AC5B7C589274618261295D95F8105BA08B5E161A83935AC0D14591E7A5358DCB4149705403BEC720283B0E8C62AB7F6E89D73CEDCDB084D2F861D290066690FF1B51B9053A0693581E6BF08DE2FCF1B60766493ED23A0DD2A4B3030F64792B438502818100ED4F7487B3B4C4C6D8A6B03CF3D602B6A16F5E2952E941CBA9A52BFC8505FDE721C50B3E286AD4F4CD72FB2D4A685630F427E3FB066A633012BFC8C7849ADDF72EC85DF99514B375B98BDDE91DDE65AA35D8D8E3E51550A7ECEE197B0089E05171CAEBB67ACE35055FE801AC11FD1F095FC80861D3B522493FE81A19B11760B7028180546CB0175EB45A4C41E6AA923AE0986E992BA4DE3D53FC32CAA5D5F31C5C5B5C4F43D97F74BA98540ABC16BDB961849A34C03437F35A4130AA64E21611B1A8E1B1EE17E7A63FDD63FFC9BA8B23FB7277699F77218A3BEF65B3D5F9F2B7D0BFDBEB05C19D3B4DA1E55C4B6DA435FD090B2260EE80D272DCF2D0FA663274A3B51102818100C2BB1A726882839E70158834DC19D72C2A188AF9F9F1326E7F0CECA5503AF4787F7B839843EE3645937930A79F9132790540DE911458A47FC442DFECE8E17416EBAABB829C54DA56562571B9632B25DDA0460AE4270D709BFB8DFE16B30070B4245E5141F24FD7AE4B5EC450808A66B778835A6D7C1DFCBE82D460E6882D18CD0281805121D177730F2F04B90912D80BB5787096D2DA38E59BB1F3D73F04DC89A00636497F686632C6DEBC33A6BC63D849C34C809AB7E38F5A3B6E99C95D134ACA9ABC18D306AB86E6186F9481037DCBF52D915B652139C4F5BDC4570C44F57F3EB08A9A5091FCDF03A0BD2A9EA26D01F4A53740D4D0ABE8BA0F3F11AD6BBF7D2E5701";

		PublicKey pub = RsaHexUtil.getPublicKey(publicKey, "10001");
		BigInteger modulus1=((RSAPublicKey)pub).getModulus();
		BigInteger exponent1=((RSAPublicKey)pub).getPublicExponent();
		System.out.println(modulus1.toString());
		System.out.println(exponent1);
		System.out.println(pub);

        String pemPublicKey = publicKeyToPem(pub);
        System.out.println("PEM格式的公钥: " + pemPublicKey);

		String temp = encrypt("admin", pub);
		System.out.println("加密后：" + temp);

		PrivateKey key = getPrivateKey(privateKey);
		System.out.println("解密后：" + decrypt(temp,key));
	}
}

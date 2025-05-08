package com.keem.kochiu.collection.util;

/**
 * 16进制rsa密钥工具
 */

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
 * 需要到http://www.bouncycastle.org下载bcprov-jdk14-123.jar。
 *
 * @author KoChiu
 */
public class RsaHexUtil {

	/**
	 * 此对象不可以每次重复创建 会造成oom
	 * 全局都是用一个单例对象
	 */
	private static BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
	static {
		Security.addProvider(bouncyCastleProvider);
	}

	/**
	 * RSA根据modulus,exponent生成公钥
	 * @param modulus
	 * @param publicExponent
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
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
	 *
	 * @param str
	 *            加密字符串
	 * @param publicKey
	 *            公钥
	 * @return 密文
	 * @throws Exception
	 *             加密过程中的异常信息
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
	 *
	 * @param key 密钥字符串（经过16进制编码）
	 * @throws Exception
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
	 * @throws NoSuchAlgorithmException
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
	 * 16进制公钥转base64
	 * @param modulus
	 * @param publicExponent
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static String hexPublicKeyToB64(String modulus, String publicExponent) throws NoSuchAlgorithmException, InvalidKeySpecException {

		PublicKey pub = RsaHexUtil.getPublicKey(modulus, publicExponent);
		byte[] encodedPublicKey = pub.getEncoded();
		String b64PublicKey = java.util.Base64.getEncoder().encodeToString(encodedPublicKey);;
		return b64PublicKey;
	}

	/**
	 * 16进制私钥转Base64
	 * @param modulus
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static String hexPrivateKeyToB64(String modulus) throws NoSuchAlgorithmException, InvalidKeySpecException {

		PrivateKey pri = RsaHexUtil.getPrivateKey(modulus);
		byte[] encodedPrivateKey = pri.getEncoded();
		String b64PrivateKey = java.util.Base64.getEncoder().encodeToString(encodedPrivateKey);;
		return b64PrivateKey;
	}

	/**
	 * 打印加密参数
	 * @param cipher
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
     * @param publicKey 公钥对象
     * @return PEM格式的公钥字符串
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

//		String password = "112233";
//		String randomFactor = "740686";
//		PublicKey pub = RsaHexUtil.getPublicKey(publicKey, "10001");
//		String temp = encrypt(password, pub);
//		String temp = encrypt(password + randomFactor, pub);
//
//		System.out.println("加密后：" + temp);
//		System.out.println("加密后长度：" + temp.length());
//		PrivateKey key = getPrivateKey(privateKey);
//		System.out.println("还原：" + decrypt(temp,key));
//		System.out.println("还原：" + decrypt(Base64.encodeBase64String(hex2Byte(temp)),key));

//		String t = "5f1af6f1c09d5db7adc14039360cff79b6e8dd19492998c1b9713242756ffb14ec3af0fece0bdad685b5c590a09a4f9e295d41a6c7481429d36eacc8e9dd3abde81e834530b4e89ffd73cba7fabf0cf2a52953b2145d98fc2f7c15e8a740089b7d79dd288e8e71208d0dd5c0d21d88d67bb6f3c4011196c5ed23fc738c889397bf6fd6452ee12f51d344024b2c85b6a2e7ebab692558d093a442f46f87c747ba8cb8c9c9472700ee12208664cbb77a9720f87ee509807f092f0d08c2137750b7969b16cb184844a1323ea86ec4d867a08198336bb1af4c032df02206a1fd6e7d739e003d4bcfbe902c7854df0b895c50db62e08bb3a5420a63936c3f6a844866";
//		String t = "2c6cb95786f145368bc1ff02bab32ad82b8801711f096c3977448fc643e635879aa789472f23e1cfff4ba83c38ba18027ad462ee34ca16a6fa757f6b2a8064059c78ba59d204632a8a2a1b1fd24ca45a2b970da70bedaa79d25009d2a38aaf2b36fc902a0520b7d089ffab5fec74ba016d39afc1cd19f764261f6304e30e3a0e7ff5619046471349a49ffb2e290b90964833d3fc8ceacd1000976714094c206ac25ed71ef1f1181cb444fc4156439337c9fa7a279888421a353b2855432339f2a25b636520971b36a5dc7d7bd4e82b80667a8c7a714a5814b3a9d7bd2fffb42e73e714466f10830cb7b11849edb407db27824b2eee227101b8bdc9d18ec5915d";
//		String t = "77224e163356ead304e4ee0522793ffa34d42752d48a790fe56c85e619ae367f973465dbfa9bcc2ad9147e39938cb9bdd2521176c43e70ba328736fc72771b87c4e5b514c121a3f40f0a58c23b7f32f39b86ac45ae0de5caeb2767c2c2f741d0a92b2dcfe58ea0ebec15f9a6554d5d84a4773116f10f8448a0cc9fb3cc0e18b56c2e6d0361e7600a98342641004fc789c5a46f87edb38ca85413ec2a662e7cf64257ad857de70e550a66202bdc996b5aa3591f96be3217f73a7f4e88c7ce5bf9c694d0da83d2edad839e94e0a62822e9142be897f939cc6ea0786130a53d4ade26a04ed2c40602e1d6c55357af89d7dabd2a6ec9e6198e9fb2cc17bdae0700d8";

//		String t = "0834496e42400ab78824d2d9f8aaae9e2bbc61b6b4a3c7f227a520b0f0970d333b3cb8aec8a1ce76e7ea696bc17bcb34f8424e724076b4deb389dd322cc65931e2757413f7702300dd45ead8808df3b37f5fdde54485652d301f552774fb0446d7270cb35ef00b36818a1b9dbb902feb981d150e5c1fb75a3d90ccdb3c8ac49cd80bfa187f3bb1be4e08bade66be67aa7d5dd5fd1b5cca5f06e07759b36a5b6cbf8793ddc2f6e6d9c8033ce2b10844dee556fc84852442db884dadc857e1629dc1b3566f395c8eeb1dcc004f502523ca3705f9c148b3c9d5207322131feeb5e2ce6848f5736418fd1bf0bfb7e7520b1bebeee208eac7f5be0241ab8d2c9f020f";
//		String t = "a15eff46f803fa3266532c29cc00085250a5dc5cf1913c37280b7cf753f21dd1fb2cd8c0e45971fe4393d2bb2856455b3ff45afd0a82c41e6d43f11d8de60b19275ec60ac76d4aeeb74b50a53b453981a154c040d8c3d93d74473453dea84f47a23dc815f7ddc772664a7f0ea49b063774494bc4ffcabab632bc74bcf1f24817b2065334b5e1eff6694464cac4cb40d77ff4ba1e8981ec7c2b7d7661c7ac2ad8df9a5df6c8661bd5aacd525075b38808edbb82f7388e0c3d6cfc205541d261291dc382ceac0e302995a840e9d629fc6446b67a2842bb45331a5e9068fffdabcbd5086fd4f9c3ca43019e5da30a960c2aa36c8c0c132014dafd00b681deb1ac26";
//		String t = "69c5289634be1d92c520e07147b29860bf1e0cac9de33678e3597960ce1b01dddebddcde41834109f83df1bd5be96db5b7c7120057789ba2a95aeed0c384a862b19fac1e4597230963d6025328d52c237379bec142f69c3d4a8219000603458c75b2e35c881f793cc5d2058ee7f2900ef410e4f4698483a06b388177699a54fa61b765970c8fc02a5c74e6402031d46a3ec256bde95381fee679cfee9fee4848d8c627b443ba7fa7540ebb9e2eada972332eb1436ac992f0e2d5a68618acb62386fa90e055db4d217e2a95ab28924879cf3c7d3aefa3930d04ecce340ca2f1a0a46db7f9022eec9634318d0e5f811b08cde5238c586b4209d752c0a535de92bd";
//		String t = "425622d2ff1ac950a8784f04cb990ec2c7196422179194456afdc5966f16b432e53f12b22e7889bcccd8aaac07c373b56b782bf0827466b2f5c00f3992bf24f6989ea26eac07862d3ab30d3a6302791f14bb933429faec4f4bd62cb372596ae8b869c76b079244dfbe9a8a32032e11127917faad93f48744b1d8b435a919c25d138b551bbeea2aeb79a086435c74e9fb40e0534669b0f00ca832666ea0a6371cb234e52cf176318828529c273b194fa6b879079205d3788a42d87b8da08468b52839cb21d59ffa385c62bbaf74ff8ed75beca163fc77ee9d670947fc8136db10abcde0330dd7954de34b3a060dd3aa5a770b009dcb465fcfe794cc1a37ac3bf7";
//		byte[] aa = HexUtils.hex2byte(t);
//		System.out.println(HexUtils.byte2hex(aa));
//		System.out.println(HexUtils.byte2hex(aa).equalsIgnoreCase(t));
//		System.out.println("还原：" + decrypt(t,key));

//		String a = byte2Hex(key.getEncoded());
//		System.out.println("私钥还原：" + a);
//		String b = byte2Hex(((RSAPublicKey)pub).getModulus().toByteArray());
//		System.out.println("公钥还原：" + b.substring(2));
//		System.out.println(a.equalsIgnoreCase(privateKey));
//		System.out.println(b.substring(2).equalsIgnoreCase(publicKey));
//
//		String[] newkeys = genKeyPair();
//		System.out.println("公钥: " + newkeys[0]);
//		System.out.println("私钥: " + newkeys[1]);
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

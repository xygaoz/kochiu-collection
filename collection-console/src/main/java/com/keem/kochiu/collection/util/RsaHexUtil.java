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
 * @author kochiu
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
		Cipher cipher = Cipher.getInstance("RSA", bouncyCastleProvider);
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
		Cipher cipher = Cipher.getInstance("RSA", bouncyCastleProvider);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		printLnAlgorithm(cipher);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;

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

	public static void main(String[] args) throws Exception{
		String publicKey = "BB14013958A4E5643A1651460CE66EE96B33DB1D6EA8DF4F894D40EBA5635A03BCD44FEB3E4E7F4A5250774C08D5A1ADCA7A561CE77627284BE34D937485C871981A58E085D6613DAF853FCBAD3A9C4B37973CB941A614623C2C5F85E6D275BA3CA10788097A13CCB843C7001D948328EFA94433C7E8F961778DCA834EF0AC7FDA7696A4A550F7344E316E7011133A415D12C18853406E0CC6B4E853681C3EE3AC0152F4DEAA00C0CB0A2C9B4E49331CEBB43CE8B55CE968E16D0D0DD7679C47966A1777E07CC3479453E8CD7BCAACED9EEDC5945C242E31A4343D5AD2CD8CD9C0E7C4B0F23CEEC6BB6D23D071EC8000AE519A412B1F48D670485EC54E16D423";
		String privateKey = "308204BF020100300D06092A864886F70D0101010500048204A9308204A50201000282010100BB14013958A4E5643A1651460CE66EE96B33DB1D6EA8DF4F894D40EBA5635A03BCD44FEB3E4E7F4A5250774C08D5A1ADCA7A561CE77627284BE34D937485C871981A58E085D6613DAF853FCBAD3A9C4B37973CB941A614623C2C5F85E6D275BA3CA10788097A13CCB843C7001D948328EFA94433C7E8F961778DCA834EF0AC7FDA7696A4A550F7344E316E7011133A415D12C18853406E0CC6B4E853681C3EE3AC0152F4DEAA00C0CB0A2C9B4E49331CEBB43CE8B55CE968E16D0D0DD7679C47966A1777E07CC3479453E8CD7BCAACED9EEDC5945C242E31A4343D5AD2CD8CD9C0E7C4B0F23CEEC6BB6D23D071EC8000AE519A412B1F48D670485EC54E16D423020301000102820101009FA3E98B7B26738DBCB7D0B0FB4F24745183D5C65EE9026EC7D6C1C094C89C46B63B62DB76B6DB8E9B8188DBBC6A38E6A9F30D4AEB057B52BAB7843A8163AC71AAFF48BA87DB31EDC6BAE97EB01B64C616F11AE367AF0693F18F581056B15FBF05FFD002793BD06B910E5D61AEDC4DB480172C59178DAA494F1113304C0BBAD685F2CEC3E9FF545905116999B64F7F374CB6533B6B879545CD0061710E0943D17EF9870A519751C9287E6BDCB71D39A75D7C9C3747C564755194F4B7E594E36D55A3C842FD337EC576D3A10FA0D41FF4D1D4410F8A4A3BC3F6DD578631853EF968792066B25B06EF6C3E745CBFEE0CBD6E47A79C1783340AC99EBA665C2C9F3902818100E1D3406411FF15CB66F2BDDACF2197E2E9AB80DEDBD1431B41ADBF422179A32C6F0B01101B479041ACB70B4A7D7C75F2FE916EC9046091CC52E9D98E62F18AAD95B5C52F3D8C18BCECE684B990E80780B3078E092B61244C690CAA7E2C00B6E2529593D4DC374F3FCD6B3EF001767AE63E5BEF4A5C4BC123B5920D5828A81EA702818100D413578A315F69025C07B955B2D6DA663244317D10B7E7F4242E02153F3EDBFD89626A1056B4AD9AD33C82C31BB50BE9D4A7C0A964BF7A5B46A394D978A4396831AB84EBF75C9F9B23BFB22A4B0AECD4C157649EB07434240CF4DA404700E8693DA1B67992CFE3F34B1FEA5D4117E5CDE6849DC955794DC86DB3982634852A2502818100B9A4031400A354D44FFE0ECAB869E2457C582B698904DF08A21665DAC4500D2EB2DE35F27AAA7AD1325D8B1FA246D5637B982C9D7D46B1B46918AA6ADDB5B88DF83B642CF088A1B175820EEBC94EA5144C98525001642B05D179C31253B406A3C8C603B001944A46A5E49AE98C1A4CEFC7A5A5F1D82BC24B4BB113DD3C16F0930281802FD308CBAE24A0EE9A4857A01ECB5AC6EF10F7C4075021ABA9B902593E40F3A53225125C0AE185C0C36002F0F2BF9A272A00866EECBC4D6066069F9B871C29A1F889805922182E19F32ED883B716889FD5D5DA67FACCC9B15386A7ED14D5FC933705CFBB8CC2FAD9B39847E6ADB630E984D1652FB0E9C103B0D16E3F375ACC4D02818100AFF255D1A9DB45BB9BF3BCE8C912025123790AA61A384FD7458752DA28BE5159582163FE3AD537A1127172ADCE071DA8CD6E0D4ED8BBF89E22CE2F90C537C423803C1D0F613DEA3273A7DF0CE841F1D0D990271E6B1EC401966EFEDB78D37AB5B5AF34C11422C5B028D6B25A4E0D0487E849AD1480D9418F65459E94A2E968C7";

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
		String[] newkeys = genKeyPair();
		System.out.println("公钥: " + newkeys[0]);
		System.out.println("私钥: " + newkeys[1]);
		PublicKey pub = RsaHexUtil.getPublicKey(newkeys[0], "10001");
		String temp = encrypt("HS0CvDz7oJn6", pub);
		System.out.println("加密后：" + temp);

		PrivateKey key = getPrivateKey(newkeys[1]);
		System.out.println("解密后：" + decrypt(temp,key));
	}
}

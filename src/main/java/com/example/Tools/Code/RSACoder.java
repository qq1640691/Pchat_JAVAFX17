package com.example.Tools.Code;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
*RSA安全编码组件
*/
public abstract class RSACoder{
  public static final String KEY_ALGORITHM = "RSA";

  /**
   * 解密<br>
   * 用私钥解密
   */
  public static byte[] decryptByPublicKey(byte[] data, String key)
      throws Exception {
    // 对密钥解密
    byte[] keyBytes = decryptBASE64(key);

    // 取得公钥
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key publicKey = keyFactory.generatePublic(x509KeySpec);

    // 对数据解密
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(Cipher.DECRYPT_MODE, publicKey);

    return cipher.doFinal(data);
  }

  /**
   * 加密<br>
   * 用公钥加密
   */
  public static byte[] encryptByPublicKey(byte[] data, String key)
      throws Exception {
    // 对公钥解密
    byte[] keyBytes = decryptBASE64(key);
    // 取得公钥
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key publicKey = keyFactory.generatePublic(x509KeySpec);
    // 对数据加密
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    return cipher.doFinal(data);
  }

  /**
   * 加密<br>
   * 用私钥加密
   */
  public static byte[] encryptByPrivateKey(byte[] data, String key)
      throws Exception {
    // 对密钥解密
    byte[] keyBytes = decryptBASE64(key);

    // 取得私钥
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

    // 对数据加密
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);

    return cipher.doFinal(data);
  }

  public static byte[] decryptBASE64(String key) {
    return Base64.getDecoder().decode(key);
  }
}

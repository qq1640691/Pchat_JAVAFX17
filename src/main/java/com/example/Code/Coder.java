package com.example.Code;

import java.util.Base64;
/**
 * 基础加密组件
 */
public abstract class Coder {
  public static byte[] decryptBASE64(String key) {
    return Base64.getDecoder().decode(key);
  }
}

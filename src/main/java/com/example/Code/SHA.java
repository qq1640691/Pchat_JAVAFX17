//单向加密

package com.example.Code;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Objects;

public class SHA {
    public static final String KEY_SHA = "SHA";
    public static String getResult(String inputStr) {
        BigInteger sha = null;
        byte[] inputData = inputStr.getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(KEY_SHA);
            messageDigest.update(inputData);
            sha = new BigInteger(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(sha).toString();
    }
}

package com.example.Tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Byteoperation {
    public static byte[] convertFileToByteArray(String fileName) throws IOException {
        BufferedInputStream bis;
        ByteArrayOutputStream bos;
        bis = new BufferedInputStream(new FileInputStream(fileName));
        bos = new ByteArrayOutputStream();
        int data;
        while ((data = bis.read()) != -1) {
            bos.write(data);
        }
        bos.flush();
        return bos.toByteArray();
    }

    public static byte[] mergeBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }
}

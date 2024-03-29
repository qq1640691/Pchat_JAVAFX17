package com.example.Client;

import com.example.Code.AES;
import com.example.thesendinf.sendinf;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.GUI.Stage.Close;
import static com.example.GUI.Stage.KEY;

public class Rchat extends Thread{

    String ip;
    int port;
    DatagramSocket Client;

    public Rchat(String ip, int port, DatagramSocket client) {
        this.ip = ip;
        this.port = port;
        Client = client;
    }

    @Override
    public void run() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleWithFixedDelay(()->{
            SocketAddress address = new InetSocketAddress(ip, port);
            long time = System.currentTimeMillis();
            File file = new File("temp\\"+time+"send.wav");
            AudioFormat audioFormat = new AudioFormat(8000.0F, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            TargetDataLine targetDataLine;
            try {
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
            try {
                targetDataLine.open(audioFormat);
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
            targetDataLine.start();
            TargetDataLine finalTargetDataLine = targetDataLine;
            new Thread(() -> {
                AudioInputStream cin = new AudioInputStream(finalTargetDataLine);
                try {
                    AudioSystem.write(cin, AudioFileFormat.Type.WAVE, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            targetDataLine.close();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendbyte(fileConvertToByteArray(file), time, Client, address);
            if(Close==0)
            {
                System.gc();
                targetDataLine.close();
                service.shutdown();
            }
        },0,1, TimeUnit.MICROSECONDS);
    }

        /**
     * 把一个文件转化为byte字节数组。
     */
    private static  byte[] fileConvertToByteArray(File file) {
        if(file.length()>0) {
            byte[] data= sendinf.filetobyte(file);
            if(file.delete())
            {
                System.out.println("ok");
            }
            return data;
        }
        return null;
    }

    public static void sendbyte(byte[] frame, long time, DatagramSocket Client, SocketAddress address)
    {
        if(frame!=null) {
            byte[] send = new byte[frame.length + 50];
            /*
              这个的标头格式,0代表是视频,i代表块数,time代表生成帧的时间 frame.length是文件大小
             */
            StringBuilder head = new StringBuilder("00" + "//" + time + "//" + frame.length + "//");
            Vchat.sendhead(head, send);
            System.arraycopy(head.toString().getBytes(StandardCharsets.UTF_8), 0, send, 0, 50);
            System.arraycopy(frame, 0, send, 50, frame.length);
            sendthebyte(Client, address, send);
        }
    }

    public static void sendthebyte(DatagramSocket Client, SocketAddress address, byte[] send) {
        byte[] sendbuf;
        try {
            sendbuf = AES.encrypt(send, KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DatagramPacket packet = new DatagramPacket(sendbuf, sendbuf.length, address);
        try {
            Client.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

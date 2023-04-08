package com.example.ClientStatic;

import com.example.Tools.Code.AESCoder;
import com.example.Tools.Code.RSACoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static com.example.GUI.TheStage.RSAprivatekey;
import static com.example.GUI.TheStage.RSApublickey;
import static com.example.NettyTCP.ChatClient.nettyclient;
import static com.example.Tools.Byteoperation.convertFileToByteArray;
import static com.example.Tools.Instrument.newkey;
import static com.example.NettyUDP.UdpClient.channelFuture;
import static com.example.Tools.Byteoperation.mergeBytes;

public class InfOperation {

    public static void Sendtheinf(String[] address,String head,byte[] content)
    {
        String aeskey = newkey();
        String heads = head+"//"+aeskey;
        try {
            /**
             * 用RSA的公钥加密,然后后面是AES加密的内容
             */
            byte[] headb = RSACoder.encryptByPrivateKey(heads.getBytes(StandardCharsets.UTF_8), RSAprivatekey);
            if(content!=null) {
                byte[] infsb = AESCoder.encrypt(content, aeskey);
                byte[] sendb = mergeBytes(headb,infsb);
                ByteBuf byteBuf = Unpooled.copiedBuffer(sendb);
                channelFuture.channel().writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(address[0].replace("/", ""), Integer.parseInt(address[1]))));
            }else {
                ByteBuf byteBuf = Unpooled.copiedBuffer(headb);
                channelFuture.channel().writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(address[0].replace("/", ""), Integer.parseInt(address[1]))));
            }
        }catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void SendtoServer(String ip,String port,String id,String token)
    {
        try {
            byte[] headb = RSACoder.encryptByPublicKey((id+"//"+token).getBytes(StandardCharsets.UTF_8), RSApublickey);
            ByteBuf byteBuf = Unpooled.copiedBuffer(headb);
            channelFuture.channel().writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(ip, Integer.parseInt(port))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean sendJB(String message,String filename,String ip,int port) {
        byte[] img;
        byte[] bm = message.getBytes(StandardCharsets.UTF_8);
        if(filename!=null) {
            try {
                img = convertFileToByteArray(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            img = new byte[0];
        }
        try {
            /**
             * 头部是String的长度和img图片的长度,256,
             * 消息是String和img的合并
             */
            byte[] sendhead = RSACoder.encryptByPublicKey((bm.length+"//"+img.length).getBytes(StandardCharsets.UTF_8), RSApublickey);
            byte[] bytemessage = mergeBytes(bm,img);
            byte[] allsend = mergeBytes(sendhead,bytemessage);
            nettyclient(allsend,ip,port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

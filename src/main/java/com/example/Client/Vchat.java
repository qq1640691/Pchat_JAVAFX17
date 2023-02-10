package com.example.Client;

import com.example.Code.AES;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.Client.showvideo.convertToFxImage;
import static com.example.GUI.Stage.*;

public class Vchat extends Thread{
    String ip;
    int port;
    DatagramSocket Client;

    public Vchat(String ip, int port, DatagramSocket client) {
        this.ip = ip;
        this.port = port;
        Client = client;
    }

    @Override
    public void run() {
        sendvideo(ip,port,Client);
    }

    public static void sendvideo(String ip,int port,DatagramSocket Client)
    {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Platform.runLater(()->{
            Stage stage = new Stage();
        SocketAddress address = new InetSocketAddress(ip,port);
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);//新建opencv抓取器，一般的电脑和移动端设备中摄像头默认序号是0，不排除其他情况
        try {
            grabber.start();//开始获取摄像头数据
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        ImageView live1 = new ImageView();
        Thread getimage = new Thread(()-> service.scheduleWithFixedDelay(()->{
            BufferedImage self_image;
            try {
                self_image = converter.getBufferedImage(grabber.grab());
            } catch (FrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ImageIO.write(resizeBufferedImage(self_image,400,300), "jpg", out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            long time  =System.currentTimeMillis();
            sendbyte(out.toByteArray(),time,Client,address);
            Image image= convertToFxImage(self_image);
            self_image.flush();
            live1.setImage(image);
    },0,1,TimeUnit.MICROSECONDS));
        getimage.start();
            HBox box = new HBox();
            box.getChildren().add(live1);
            Scene scene = new Scene(box,grabber.getImageWidth(),grabber.getImageHeight());
            stage.setScene(scene);
            stage.setX(100);
            stage.setY(500);
            stage.show();
            File ico = new File("ico\\video.png");
        try {
            stage.getIcons().add(new Image(new FileInputStream(ico)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
            stage.setResizable(false);
            Thread rchat = new Rchat(ip,port,Client);
            rchat.start();
            stage.setOnCloseRequest(event -> {
                rchat.interrupt();
                service.shutdown();
                showvideo="allow";
                Close=0;
                getimage.interrupt();
                try {
                    grabber.close();
                } catch (FrameGrabber.Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public static void sendbyte(byte[] frame, long time, DatagramSocket Client, SocketAddress address)
    {
        byte[] send = new byte[1000];
        int times = (frame.length/send.length)+1;
        byte[] end = new byte[frame.length%send.length+50];

        for(int i=0;i<times;i++)
        {
            if (i>=times-1) {
                /*
                  这个的标头格式,0代表是视频,i代表块数,time代表生成帧的时间 frame.length是文件大小
                 */
                sendtheimage(frame, time, end, i);
                Rchat.sendthebyte(Client, address, end);
            }
            else if(i<times-1){
                sendtheimage(frame, time, send, i);
                byte[] sendbuf;
                try {
                    sendbuf = AES.encrypt(send, KEY);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                DatagramPacket packet = new DatagramPacket(sendbuf, sendbuf.length, address);
                try {
                    Client.send(packet);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void sendtheimage(byte[] frame, long time, byte[] end, int i) {
        StringBuilder head = new StringBuilder("01" + "//" + i + "//" + time + "//" + frame.length + "//");
        sendhead(head,end);
        System.arraycopy(head.toString().getBytes(StandardCharsets.UTF_8), 0, end, 0, 50);
        System.arraycopy(frame, i * 950, end, 50, end.length - 50);
    }

    private static BufferedImage resizeBufferedImage(BufferedImage source, int targetW, int targetH) {
		int type = source.getType();
		BufferedImage target;
		double sx = (double) targetW / source.getWidth();
		double sy = (double) targetH / source.getHeight();
		if (sx > sy) {
			sx = sy;
			targetW = (int) (sx * source.getWidth());
		} else if(sx <= sy){
			sy = sx;
			targetH = (int) (sy * source.getHeight());
		}
		if (type == BufferedImage.TYPE_CUSTOM) { // handmade
			ColorModel cm = source.getColorModel();
			WritableRaster raster = cm.createCompatibleWritableRaster(targetW, targetH);
			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
			target = new BufferedImage(cm, raster, alphaPremultiplied, null);
		} else {
			target = new BufferedImage(targetW, targetH, type);
		}
		Graphics2D g = target.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
		g.dispose();
		return target;
	}

    public static void sendhead(StringBuilder packetinf,byte[] sendbuf)
    {
        if (packetinf.toString().getBytes(StandardCharsets.UTF_8).length<50)
        {
            int templength = 50- packetinf.toString().getBytes(StandardCharsets.UTF_8).length;
            packetinf.append("*".repeat(Math.max(0, templength)));
        }
        System.arraycopy(packetinf.toString().getBytes(StandardCharsets.UTF_8),0,sendbuf,0,50);//把头部复制到sendbuf数组里
    }


}

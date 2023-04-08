package com.example.ClientTherad;

import com.example.ClientStatic.InfOperation;
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
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import static com.example.ClientStatic.InfOperation.Sendtheinf;
import static com.example.ClientTherad.showvideo.convertToFxImage;
import static com.example.Tools.Instrument.IMAGEZIP;

public class Myimage extends Thread{
    String address;

    public Myimage(String address) {
        this.address = address;
    }


    @Override
    public void run() {
        sendvideo(address.split("//"));
    }

    public static void sendvideo(String[] theaddress)
    {
        Thread RC = new MyVoice(theaddress);
        RC.start();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Platform.runLater(()->{
            Stage stage = new Stage();
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
                ImageIO.write(IMAGEZIP(self_image, 0.2F), "jpg", out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            /**
             * 这句话是发送视频流
             */
            Sendtheinf(theaddress,"video", out.toByteArray());
            Image image= convertToFxImage(self_image);
            self_image.flush();
            live1.setImage(image);
    },0,40,TimeUnit.MILLISECONDS));
        getimage.start();
            HBox box = new HBox();
            box.getChildren().add(live1);
            Scene scene = new Scene(box,grabber.getImageWidth(),grabber.getImageHeight());
            stage.setScene(scene);
            stage.show();
            File ico = new File("ico\\video.png");
            try {
                stage.getIcons().add(new Image(new FileInputStream(ico)));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            stage.setResizable(false);
            stage.setOnCloseRequest(event->{
                try {
                    grabber.close();
                } catch (FrameGrabber.Exception e) {
                    throw new RuntimeException(e);
                }
                service.shutdownNow();
                getimage.interrupt();
                RC.interrupt();
            });
        });
    }

}

class MyVoice extends Thread{
    String[] address;
    public MyVoice(String[] address) {
        this.address = address;
    }
    @Override
    public void run() {
        //读取麦克风数据
        AudioFormat audioFormat = new AudioFormat(8000.0F, 16, 1, true, false);
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
        final int bufSize = 4096;
        byte[] buffer = new byte[bufSize];
        while ((targetDataLine.read(buffer, 0, bufSize)) > 0) {
            //直接发送麦克风数据流
            InfOperation.Sendtheinf(address,"audio",buffer);
            if(Thread.interrupted())
            {
                targetDataLine.close();
                return;
            }
        }
    }
}

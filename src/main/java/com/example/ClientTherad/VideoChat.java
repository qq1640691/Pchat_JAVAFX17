package com.example.ClientTherad;

import com.example.Main;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.GUI.TheStage.audio;
import static com.example.GUI.TheStage.video;
import static com.example.Tools.JavaSound.Acoustics.playaudio;
import static com.example.Tools.Instrument.IMAGEZIP;

public class VideoChat extends Thread{
    @Override
    public void run() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleWithFixedDelay(()->{
            if(video.size()>0)
            {
                Thread showvideo=new showvideo();
                showvideo.start();
                service.shutdownNow();
            }
        },0,1, TimeUnit.SECONDS);
    }
}

class showvideo extends Thread{
    @Override
    public void run() {
        Thread showaudio = new showaudio();
        showaudio.start();
        Platform.runLater(()-> {
            Stage stage = new Stage();
            ImageView live1 = new ImageView();
            Thread thread = new Thread(()->{
                Image image;
                while (true) {
                    if (video.size() > 0) {
                        image = getvideo(video.get(0));
                        video.remove(0);
                        live1.setImage(image);
                    }
                }
            });
            thread.start();
            HBox hBox = new HBox();
            hBox.getChildren().add(live1);
            Scene scene = new Scene(hBox,640,480);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
            stage.setOnCloseRequest(e->{
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                audio.clear();
                video.clear();
                Main.LIVE =0;
                showaudio.interrupt();
            });
            File ico = new File("ico\\video.png");
            try {
                stage.getIcons().add(new Image(new FileInputStream(ico)));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static Image getvideo(byte[] imageInByte)
    {
        if(imageInByte.length>0)
        {
            InputStream in = new ByteArrayInputStream(imageInByte);
            try {
                BufferedImage bImageFromConvert = ImageIO.read(in);
                if(bImageFromConvert!=null)
                {
                  return convertToFxImage(IMAGEZIP(bImageFromConvert,5F));
                }
                else {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        return new ImageView(wr).getImage();
    }

}

class showaudio extends Thread{

    AudioFormat format = new AudioFormat(8000.0F, 16, 1, true, false);
    @Override
    public void run() {
        while(true) {
            if (audio.size() > 0) {
                byte[] get = audio.get(0);
                InputStream is = new ByteArrayInputStream(audio.get(0));
                new Thread(() -> {
                    try {
                        playaudio(is, format);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                audio.remove(get);
            }
            if(Thread.interrupted())
            {
                return;
            }
        }
    }
}

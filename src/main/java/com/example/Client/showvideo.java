package com.example.Client;

import com.example.fileoperate.showfile;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.GUI.Stage.*;

public class showvideo extends Thread{
    @Override
    public void run() {
        Platform.runLater(()->{
            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            ImageView live1 = new ImageView();
            Stage stage = new Stage();
            HBox box = new HBox();
            box.getChildren().add(live1);
            Scene scene = new Scene(box, 400, 300);
            stage.setScene(scene);
            videostrem.clear();
                showedtime.clear();
                longtime.clear();
            Thread getimage=new Thread(()-> service.scheduleWithFixedDelay(()->{
                for(String time:longtime)
                {
                    if(videostrem.containsKey(time))
                    {
                        long dt = videostrem.get(time).length/1000;
                        try {
                            Thread.sleep(dt);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        byte[] showimage = videostrem.get(time);
                        Image image = getvideo(showimage);
                        live1.setImage(image);
                        videostrem.remove(time);
                        longtime.remove(time);
                        showedtime.add(time);
                    }
                }
            },0, 1, TimeUnit.MICROSECONDS));
            getimage.start();
            File ico = new File("ico\\video.png");
        try {
            stage.getIcons().add(new Image(new FileInputStream(ico)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
            stage.show();
            stage.setX(100);
            stage.setY(100);
            stage.setResizable(false);
            Thread showvc = new showvc();
            showvc.start();
            stage.setOnCloseRequest(event -> {
                service.shutdown();
                sendvideo="allow";
                videostrem.clear();
                showedtime.clear();
                showvc.interrupt();
                longtime.clear();
                getimage.interrupt();
                showfile.delecttemp();
            }
            );
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
                  return convertToFxImage(bImageFromConvert);
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

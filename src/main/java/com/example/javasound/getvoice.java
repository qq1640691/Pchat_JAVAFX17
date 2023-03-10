package com.example.javasound;

import com.example.Client.sendfile;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentHashMap;

public class getvoice {
    //空参也可以吧,直接在里面新建
    public static void record(DatagramSocket client,String title, ConcurrentHashMap<String, String> thefilepath)
            throws LineUnavailableException, InterruptedException {
        File outputFile = new File("record\\"+System.currentTimeMillis()+".wav");
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 16, 2, 4, 8000.0F, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        new Thread(() -> {
            AudioInputStream cin = new AudioInputStream(targetDataLine);
            try {
                AudioSystem.write(cin, AudioFileFormat.Type.WAVE, outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Platform.runLater(()->{
            Stage alert = new Stage();
            Text err = new Text();
            err.setText("正在录音,关闭停止录音");
            com.example.GUI.Stage.newalert(alert, err);
            alert.setOnCloseRequest(e->{
                targetDataLine.close();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                Thread sendvoice = new sendfile(client,outputFile,title,thefilepath,"fzvoice");
                sendvoice.start();
            });
        });
    }
}

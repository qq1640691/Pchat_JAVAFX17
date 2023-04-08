package com.example.Tools.JavaSound;

import com.example.ClientTherad.SendFile;
import com.example.GUI.TheStage;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class Acoustics {
    public static void record(String title, ConcurrentHashMap<String, String> thefilepath)
            throws LineUnavailableException, InterruptedException {
        File outputFile = new File("temp\\"+System.currentTimeMillis()+".wav");
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
            TheStage.newalert(alert, err);
            alert.setOnCloseRequest(e->{
                targetDataLine.close();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                Thread sendvoice = new SendFile(outputFile,title,thefilepath,"voice");
                sendvoice.start();
            });
        });
    }

    public static void playfile(String file) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(file));
            AudioFormat audioFormat = audioInputStream.getFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            byte[] b = new byte[1024];
            int len;
            sourceDataLine.open(audioFormat, 1024);
            sourceDataLine.start();
            while ((len = audioInputStream.read(b)) != -1) {
                sourceDataLine.write(b, 0, len);
            }
            audioInputStream.close();
            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("error");
        }
    }

    public static void playaudio(InputStream stream, AudioFormat format) throws IOException {
        AudioInputStream audioInputStream = new AudioInputStream(stream, format, AudioSystem.NOT_SPECIFIED);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line;
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        byte[] buffer = new byte[4096];
        int n = 0;
        while (n != -1) {
            n = audioInputStream.read(buffer, 0, buffer.length);
            if (n > 0) {
                line.write(buffer, 0, n);
            }
        }
        line.drain();
        line.stop();
        line.close();
        audioInputStream.close();
    }
}

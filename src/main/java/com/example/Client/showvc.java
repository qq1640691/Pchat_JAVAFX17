package com.example.Client;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.GUI.Stage.*;

public class showvc extends Thread{
    @Override
    public void run() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleWithFixedDelay(()->{
            for (String s:videostremv.keySet()) {
                File file = null;
                if (videostremv.get(s) != null) {
                    convertByteArrayToFile(videostremv.get(s), s);
                    file = new File("temp\\" + s + "receive.wav");
                    try {
                        playwav.play("temp\\" + s + "receive.wav");
                    } catch (Exception e) {
                        infarea.appendText("错误内容,无法播放\n");
                    }
                }
                if (file != null) {
                    if (file.delete())
                    {
                        System.out.println("临时文件删除");
                    }
                }
                videostremv.remove(s);
            }
        },0,1, TimeUnit.MICROSECONDS);
    }

   public static void convertByteArrayToFile(byte[] arr,String s) {
        try (
                BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(arr));
                FileOutputStream fileOutputStream = new FileOutputStream("temp\\"+s+"receive.wav");
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)
        ) {
            int data;
            while ((data = bis.read()) != -1) {
                bos.write(data);
            }
            bos.flush();
        } catch (IOException e) {
            System.out.println("error");
        }
    }
}

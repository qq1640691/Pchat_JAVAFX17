package com.example.ClientTherad;

import com.example.ClientStatic.FileOperation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.ClientStatic.InfOperation.Sendtheinf;
import static com.example.GUI.TheStage.infarea;
import static com.example.Tools.Instrument.sendthedelay;

/**
 * 发送文件的线程
 */
public class SendFile extends Thread {
    File file;
    String title;
    ConcurrentHashMap<String, String> thefilepath;
    String type;

    public SendFile(File file, String title, ConcurrentHashMap<String, String> thefilepath, String type) {
        this.file = file;
        this.title = title;
        this.thefilepath = thefilepath;
        this.type = type;
    }

    @Override
    public synchronized void run() {
        String[] infs = title.split("//");
        System.out.println("类型:"+type);
        try {
            if(type.equals("voice"))
            {
                Sendtheinf(infs,"messageone//"+System.currentTimeMillis(),"正在向您发送语音:".getBytes(StandardCharsets.UTF_8));
            }
            if(type.equals("image")){
                Sendtheinf(infs,"messageone//"+System.currentTimeMillis(),"正在向您发送图片:".getBytes(StandardCharsets.UTF_8));
            }
            if(type.equals("file"))
            {
                Sendtheinf(infs,"messageone//"+System.currentTimeMillis(),("正在向您发送文件"+file.getName()).getBytes(StandardCharsets.UTF_8));
                infarea.appendText("正在发送文件"+file.getName()+"预计时间"+(double)file.length()/1000/1700*20+"\n");
            }
            int delay;
            try {
                delay = Integer.parseInt(sendthedelay(infs[0].replace("/","")));
            } catch (Exception e) {
                delay=15;
            }
            FileOperation.sendbigfiles(infs, file, thefilepath,delay,type);
            Thread.sleep(delay);
            FileOperation.sendFF(infs,type+"//"+file.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

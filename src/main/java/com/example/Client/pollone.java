package com.example.Client;

import com.example.fileoperate.showfile;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.net.DatagramSocket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.Client.Chat.dealpacketone;
import static com.example.GUI.Stage.allbyte;

public class pollone {
    ObservableList<Object> getdata;
    ListView<Object> getlist;
    String title;
    CopyOnWriteArrayList<String> mess;

    DatagramSocket Client;//绑定的ip和端口


    public pollone(ObservableList<Object> getdata, ListView<Object> getlist, String title, CopyOnWriteArrayList<String> mess, DatagramSocket client) {
        this.getdata = getdata;
        this.getlist = getlist;
        this.title = title;
        this.mess = mess;
        Client = client;
    }

    ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
    public void poll()
    {
        service.scheduleWithFixedDelay(()-> showfile.listshowimage("image\\"+title.split("//")[2], getdata,getlist),10,5, TimeUnit.SECONDS);

        service.scheduleWithFixedDelay(()-> showfile.listshowvoice("voice\\"+title.split("//")[2],getdata,getlist),10,5, TimeUnit.SECONDS);


        service.scheduleWithFixedDelay(()->{
            for (byte[] result : allbyte) {
                    String object;
                    if (result.length >= 256) {
                        object = new String(result, 0, 256);
                    } else {
                        object = new String(result, 0, result.length);
                    }
                    if (object.contains(title) && !object.contains("list/") && !object.contains("mesl/")) {
                        try {
                            dealpacketone(Client,result,title, getdata, getlist,mess);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        allbyte.remove(result);
                    }
                }
        },0,1, TimeUnit.MILLISECONDS);
    }

    public void close()
    {
        service.shutdown();
    }
}

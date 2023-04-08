package com.example.ClientTherad;

import com.example.GUI.TheStage;
import com.example.Tools.Filtration.FileUntil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.ClientTherad.Scheduled_Tasks_one.printmessageone;
import static com.example.ClientStatic.InfOperation.Sendtheinf;
import static com.example.GUI.TheStage.infarea;

public class SendMessageToOne extends Thread {
    TextArea area;
    String inf;
    ObservableList<Object> getdata;
    ListView<Object> getlist;
    ConcurrentHashMap<String,String> mess;

    public SendMessageToOne(TextArea area, String inf, ObservableList<Object> getdata, ListView<Object> getlist, ConcurrentHashMap<String, String> mess) {
        this.area = area;
        this.inf = inf;
        this.getdata = getdata;
        this.getlist = getlist;
        this.mess = mess;
    }

    @Override
    public void run() {
        String input = area.getText();
        if(FileUntil.Filter(input))
        {
            infarea.appendText("语句中含有违法信息,请修改后发送\n");
            return;
        }
        if(!Objects.equals(input, ""))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date time = new Date(); // 获取当前时间
            String format = sdf.format(time);// 格式化时间
            String[] oneuser = inf.split("//");
            String linuxtime = String.valueOf(System.currentTimeMillis());
            Platform.runLater(()->{
                if(input.getBytes(StandardCharsets.UTF_8).length>=3000)
                {
                    infarea.appendText("消息过长,请分段发送");
                }
                //更新JavaFX的主线程的代码放在此处
                else if(!input.equals("")){
                    if (mess.size() != 0) {
                        for (String s : mess.keySet()) {
                           Sendtheinf(oneuser,"messageone//"+linuxtime,mess.get(s).getBytes(StandardCharsets.UTF_8));//标记当前的时间
                        }
                    }
                    mess.put(linuxtime,input);
                    Sendtheinf(oneuser,"messageone//"+linuxtime,input.getBytes(StandardCharsets.UTF_8));
                    getdata.add(TheStage.ID + ":" + format);
                    printmessageone(input, getdata, getlist);
                    area.setText("");
               }
            });
        }
    }
}

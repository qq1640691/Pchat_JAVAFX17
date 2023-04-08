package com.example.ClientTherad;

import com.example.GUI.TheStage;
import com.example.Tools.Filtration.FileUntil;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.ClientTherad.Scheduled_Tasks_one.printmessageone;
import static com.example.ClientStatic.InfOperation.Sendtheinf;
import static com.example.GUI.TheStage.*;
import static com.example.Tools.Instrument.myinf;

/**
 * 发送消息的线程,重写一下线程池,变成广播消息.
 */
public class SendMessageToAll extends Thread {
    CopyOnWriteArrayList<String> userlist;
    TextArea area;

    public SendMessageToAll(CopyOnWriteArrayList<String> userlist, TextArea area) {
        this.userlist = userlist;
        this.area = area;
    }

    @Override
    public void run() {
        String input = area.getText();
        if (!Objects.equals(input, "")) {
            if(FileUntil.Filter(input))
            {
                infarea.appendText("语句中含有违法信息,请修改后发送\n");
                return;
            }
            ExecutorService pool = Executors.newFixedThreadPool(100);
            if(input.getBytes(StandardCharsets.UTF_8).length>=1500)
            {
                infarea.appendText("消息过长,请分段发送");
            }
            else {
                for (String use : userlist) {
                    pool.submit(() -> {
                        if (!Objects.equals(use, myinf())) {
                            String[] oneuser = use.split("//");
                            Sendtheinf(oneuser,"messageall",input.getBytes(StandardCharsets.UTF_8));
                        }
                    });
                }
            }
            SimpleDateFormat sdfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date timed = new Date(); // 获取当前时间
            String formattime = sdfd.format(timed);// 格式化时间
            Platform.runLater(() -> {
                //更新JavaFX的主线程的代码放在此处
                if (!input.equals("")&&input.getBytes(StandardCharsets.UTF_8).length<800) {
                    getdata.add(TheStage.ID + ":" + formattime);
                    printmessageone(input, getdata, getlist);
                    area.setText("");
                }
            });
        }
    }
}

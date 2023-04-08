package com.example.ClientTherad;

import com.example.ClientStatic.InfOperation;
import com.example.GUI.TheStage;
import com.example.Start;
import com.example.Tools.Code.RSACoder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.ClientStatic.InfOperation.SendtoServer;
import static com.example.GUI.TheStage.*;
import static com.example.NettyTCP.ChatClient.nettyclient;
import static com.example.Tools.Instrument.*;

public class Scheduled_Tasks {
    TextArea area;
    String ip;
    int port;
    String id;
    ConcurrentHashMap<String, String> thefilepath;
    ObservableList<Object> infdata;
    String token;

    public Scheduled_Tasks(TextArea area, String ip, int port, String id, ConcurrentHashMap<String, String> thefilepath, ObservableList<Object> infdata, String token) {
        this.area = area;
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.thefilepath = thefilepath;
        this.infdata = infdata;
        this.token = token;
    }

    public void Scheduled_Tasks_pool() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(6);
        service.scheduleWithFixedDelay(()->{
            try {
            nettyclient(RSACoder.encryptByPublicKey(getCpuId().getBytes(StandardCharsets.UTF_8),RSApublickey),IP,27777);
            Thread.sleep(10000);
            if(Objects.equals(Start.RECEIVE, "NO"))
            {
                    thealert("对不起,您在黑名单中,无法使用本软件",0);
            }
        } catch (Exception e) {
                thealert("对不起,系统异常",0);
            }
        },10,600,TimeUnit.SECONDS);
        service.scheduleWithFixedDelay(() ->
        {
            if (userdelay.size() == 0&&userlist.size()>1) {
                area.appendText("正在与其他用户连接中,请耐心等待\n");
            }
            if (userdelay.size() > 0) {
                area.appendText("已连接接用户:\n");
                area.appendText(userdelay + "\n");
            }
        }, 10, 30, TimeUnit.SECONDS);

        service.scheduleWithFixedDelay(() ->
        {
            if (filetopart.size() > 0) {
                Set<String> key = filetopart.keySet();
                for (String str : key) {
                    if (filetopart.get(str).size()  != 0) {
                        String time = "正在接收" + str.split("//")[1] + ",剩余" + filetopart.get(str).size() + "块";
                        Platform.runLater(() -> infarea.appendText(time + "\n"));
                    }else {
                        String string = str.split("//")[1]+"接受完成";
                        Platform.runLater(() -> infarea.appendText(string + "\n"));
                        filetopart.remove(str);
                    }
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

        service.scheduleWithFixedDelay(() ->
        {
            if (userlist.size() > 1) {
                for (String str : userlist) {
                    if (!Objects.equals(myinf(), str)) {
                        InfOperation.Sendtheinf(str.split("//"),"delay",null);
                    }
                }
            }
        }, 5, 30, TimeUnit.SECONDS);

        service.scheduleWithFixedDelay(()-> SendtoServer(ip, String.valueOf(port),id,token),0,55,TimeUnit.SECONDS);
        service.schedule(()->{
            if(userlist.size()==0)
            {
                Platform.runLater(()->
                {
                    Stage aleat = new Stage();
                    Text err = new Text();
                    err.setText("抱歉,服务器暂未运行");
                    TheStage.newalert(aleat, err);
                    all.close();
                    aleat.setOnCloseRequest(event -> System.exit(0));
                });
            }
        },10,TimeUnit.SECONDS);
    }
}
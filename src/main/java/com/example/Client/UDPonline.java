package com.example.Client;


import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static com.example.GUI.Stage.all;
import static com.example.GUI.Stage.userlist;
public class UDPonline extends Thread{

    @Override
    public synchronized void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(userlist.size()==0)
        {
            Platform.runLater(()->
            {
                Stage aleat = new Stage();
                Text err = new Text();
                err.setText("抱歉,服务器暂未运行");
                com.example.GUI.Stage.newalert(aleat, err);
                all.close();
                aleat.setOnCloseRequest(event -> System.exit(0));
            });
        }
    }
}

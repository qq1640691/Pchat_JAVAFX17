package com.example;

import com.example.GUI.TheStage;
import javafx.application.Application;

import javafx.stage.Stage;

import static com.example.Start.Flag;
import static com.example.Tools.Instrument.thealert;
import static com.example.GUI.TheStage.*;

public class Main extends Application {
    public static long interval=0L;
    public static int LIVE=0;
    @Override
    public void start(Stage primaryStage) {
        if(Flag==1)
        {
            thealert("配置下载失败,无法登录",0);
        }
        else {
            RSApublickey="公钥1024位";
            RSAprivatekey="私钥";
            TheStage.login();
        }
    }
}

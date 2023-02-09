package com.example;

import com.example.Client.receivepacket;
import com.example.natserver.natserver;
import javafx.application.Application;
import javafx.stage.Stage;

import java.net.DatagramSocket;

public class Main extends Application {

//    public static void main(String[] args) {
//        launch(args);
//    }

    @Override
    public void start(Stage primaryStage) {
        DatagramSocket Client = natserver.socket();
        Thread receivepacket = new receivepacket(Client);
        receivepacket.start();
        com.example.GUI.Stage.login(Client);
    }
}

package com.example;

import com.example.Tools.Filtration.FileUntil;
import com.example.Tools.Filtration.thewordList;
import javafx.application.Application;

import static com.example.Tools.Instrument.Download;


public class Start {
    public static thewordList wordList;
    public static int Flag;
    public static String RECEIVE = "NO";

    public static void main(String[] args) {
        /**
         * 先发送自己的CPUID和用户信息
         * 先下载,然后加载敏感词库,然后删除敏感词库
         */
        /**
         *从网上下载词库,如果下载不到,就一直卡着?
         */
        if(Download("http://47.113.189.105:35555/keywords.txt"))
        {
            Flag=0;
            FileUntil.loadWordFromFile("./keywords");//先预加载词库
            Application.launch(Main.class,args);
        }
        else {
            Flag=1;
            Application.launch(Main.class,args);
        }

    }
}



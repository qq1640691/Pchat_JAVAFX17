package com.example.ClientTherad;

import com.example.ClientStatic.InfOperation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

import static com.example.GUI.TheStage.solomessage;
import static com.example.GUI.TheStage.solomessageget;

public class Scheduled_Tasks_one {
    ObservableList<Object> getdata;
    ListView<Object> getlist;
    String title;
    ConcurrentHashMap<String,String> mess;
    CopyOnWriteArrayList<String> showdfile = new CopyOnWriteArrayList<>();

    public Scheduled_Tasks_one(ObservableList<Object> getdata, ListView<Object> getlist, String title, ConcurrentHashMap<String, String> mess) {
        this.getdata = getdata;
        this.getlist = getlist;
        this.title = title;
        this.mess = mess;
    }

    ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
    public void Scheduled_Tasks_poll()
    {
        service.scheduleWithFixedDelay(()->{
            Set<String> keySet = solomessage.keySet();
            for (String username:keySet) {
                    if (username.contains(title)) {
                        String[] name = title.split("//");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date time = new Date(); // 获取当前时间
                        String format = sdf.format(time);// 格式化时间
                        String getmessage = solomessage.get(username);
                        Platform.runLater(() ->
                        {
                            getdata.add(name[2] + ":" + format);
                            printmessageone(getmessage, getdata, getlist);
                            /**
                             * 返回一个message确认.
                             */
                            InfOperation.Sendtheinf(username.split("//"),"messageget", username.split("//")[3].getBytes(StandardCharsets.UTF_8));
                        });
                        solomessage.remove(username,getmessage);
                    }
                }
        },0,1, TimeUnit.MILLISECONDS);

        service.scheduleWithFixedDelay(()->{
            for(String s:mess.keySet())
            {
                for (String k:solomessageget)
                {
                    if (Objects.equals(k, s))
                    {
                        mess.remove(s);
                        solomessageget.remove(s);
                    }
                }
            }
        },0,1,TimeUnit.MILLISECONDS);

        service.scheduleWithFixedDelay(()->{
            File folder = new File("Receive"+"\\"+title.split("//")[2]+"\\"+"image");//文件夹路径
            File[] listOfFiles = folder.listFiles();
            if(listOfFiles!=null) {
                for (int i = 0; i < listOfFiles.length; i++) {
                    int finalI = i;
                    if(!showdfile.contains(listOfFiles[i].getName())&&!listOfFiles[i].getName().endsWith("tmp")) {
                        Platform.runLater(() -> {
                            FileInputStream input;
                            try {
                                input = new FileInputStream(listOfFiles[finalI]);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            Image image = new Image(input);
                            ImageView imageView = new ImageView(image);
                            double bl = image.getHeight() / image.getWidth();
                            imageView.setFitHeight(450 * bl);
                            imageView.setFitWidth(450);
                            getdata.add(imageView);
                            getlist.setItems(getdata);
                            try {
                                input.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            showdfile.add(listOfFiles[finalI].getName());
                            listOfFiles[finalI].renameTo(new File("Receive" + "\\" + title.split("//")[2] + "\\" + "image" + "\\" + "SHOWED" + listOfFiles[finalI].getName()));
                        });
                    }
                }
            }
        },0,1,TimeUnit.SECONDS);

        service.scheduleWithFixedDelay(()->{
            File folder = new File("Receive"+"\\"+title.split("//")[2]+"\\"+"voice");//文件夹路径
            File[] listOfFiles = folder.listFiles();
            if(listOfFiles!=null) {
                for (int i = 0; i < listOfFiles.length; i++) {
                    int finalI = i;
                    if(!showdfile.contains(listOfFiles[i].getName())&&!listOfFiles[i].getName().endsWith("tmp")) {
                        Platform.runLater(()->{
                            getdata.add(String.valueOf(listOfFiles[finalI]));
                            getlist.setItems(getdata);
                            showdfile.add(listOfFiles[finalI].getName());
                            listOfFiles[finalI].renameTo(new File("Receive" + "\\" + title.split("//")[2] + "\\" + "voice" + "\\" + "SHOWED" + listOfFiles[finalI].getName()));
                        });
                    }

                }
            }
        },0,1,TimeUnit.SECONDS);
    }

    public static void printmessageone(String getmessage, ObservableList<Object> getdata, ListView<Object> getlist) {
        TextArea print = new TextArea();
        print.setText(getmessage);
        print.setWrapText(true);
        print.setEditable(false);
        print.setPrefSize(500, 100);
        print.setStyle("-fx-font-size: 18 ;-fx-font-weight:bold");
        getdata.add(print);
        getlist.setItems(getdata);
    }

    public void close()
    {
        service.shutdown();
    }
}

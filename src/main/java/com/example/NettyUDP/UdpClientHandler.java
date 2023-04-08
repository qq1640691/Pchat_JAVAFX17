package com.example.NettyUDP;

import com.example.ClientStatic.InfOperation;
import com.example.ClientTherad.Myimage;
import com.example.Tools.Code.AESCoder;
import com.example.Tools.Code.RSACoder;
import com.example.GUI.TheStage;
import com.example.Main;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.ClientTherad.Scheduled_Tasks_one.printmessageone;
import static com.example.GUI.TheStage.*;
import static com.example.ClientStatic.FileOperation.*;
import static com.example.Tools.Instrument.thealert;

/**
 * 传输过来bytes的结构应该怎么写?
 * 前256位加密一下?里面存点信息?
 */

public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    /**
     * 接收服务器用户,接受单人消息,接收群聊消息都写好了
     */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
            ByteBuf byteBuf = msg.content();
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            byte[] headb = new byte[256];
            System.arraycopy(bytes,0,headb,0,256);
            String head;
            byte[] content = new byte[bytes.length-256];
            System.arraycopy(bytes,256,content,0,content.length);
            try {
                head = new String(RSACoder.decryptByPublicKey(headb,RSApublickey));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            /**
             * 我的思路是,当我打开视频聊天时,先向对方发送一个want,如果对方的live是0,就向我回传一个allow,否则回传turndown?
             */
            if(head.startsWith("want"))
            {
                String username = "用户//+temp/"+msg.sender();
                if(Main.LIVE==0)
                {
                    for (String user : userlist) {
                        if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                            username = user;
                        }
                    }
                    String finalUsername = username;
                    Platform.runLater(()->{
                        Stage aleat = new Stage();
                        Text err = new Text();
                        err.setText("对方想与您通话");
                        err.setTextAlignment(TextAlignment.CENTER);
                        err.setFill(Color.RED);
                        err.setFont(Font.font(null, FontWeight.BOLD, 18));
                        Button allow = new Button("允许");
                        allow.setOnAction(event -> {
                            InfOperation.Sendtheinf(finalUsername.split("//"), "allow", null);
                            Main.LIVE = 1;
                            Thread Myimage= new Myimage(finalUsername);
                            Myimage.start();
                            aleat.close();
                        });
                        allow.setMinWidth(30);
                        allow.getStylesheets().add("the.css");
                        Button no = new Button("拒绝");
                        no.getStylesheets().add("the.css");
                        no.setOnAction(event -> {
                            InfOperation.Sendtheinf(finalUsername.split("//"),"turndown",null);
                            aleat.close();
                        });
                        VBox box = new VBox();
                        HBox boxn = new HBox();
                        boxn.setMinHeight(20);
                        box.getChildren().add(boxn);
                        HBox box1 = new HBox();
                        err.setTextAlignment(TextAlignment.CENTER);
                        box1.setAlignment(Pos.BASELINE_CENTER);
                        box1.getChildren().add(err);
                        HBox box2 = new HBox();
                        box.getChildren().add(box1);
                        box.getChildren().add(box2);
                        box1.setMinHeight(50);
                        box2.setAlignment(Pos.BASELINE_CENTER);
                        box2.getChildren().add(allow);
                        box2.getChildren().add(no);
                        Scene scene = new Scene(box,200,150);
                        aleat.setScene(scene);
                        aleat.setResizable(false);
                        File ico = new File("ico\\solomessage.png");
                        try {
                            aleat.getIcons().add(new Image(new FileInputStream(ico)));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        aleat.show();
                    });
                }
                else {
                    for (String user : userlist) {
                        if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                            username = user;
                        }
                    }
                    InfOperation.Sendtheinf(username.split("//"),"turndown",null);
                }
            }
            if(head.startsWith("allow"))
            {
                String username = "用户//+temp/"+msg.sender();
                    for (String user : userlist) {
                        if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                            username = user;
                        }
                    }
                Thread Myimage = new Myimage(username);
                Myimage.start();
            }
            if(head.startsWith("turndown"))
            {
                /**
                 * 对方正在通话,不允许新建通话
                 */
                thealert("对方拒绝与您通话,请稍后再试",1);
            }
            if(head.startsWith("video"))
            {
                /**
                 * 视频,还是要放到鸡巴数组里,草
                 */
                try {
                    byte[] get = AESCoder.decrypt(content,head.split("//")[1]);
                    video.add(get);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if(head.startsWith("audio"))
            {
                try {
                    byte[] get = AESCoder.decrypt(content,head.split("//")[1]);
                    audio.add(get);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            /**
             * 如果是从服务器回传回来的用户列表;
             */
            if (String.valueOf(msg.sender().getAddress()).replace("/", "").equals(IP)&&String.valueOf(msg.sender().getPort()).equals(String.valueOf(PORT)))
            {
                try {
                    /**
                     * /255.255.255.255:65536//NAME
                     */
                    if(!userlist.contains(head))
                    {
                        userlist.add(head);
                    }
                    Platform.runLater(() -> {
                        TheStage.listView.getItems().clear();
                        TheStage.data.addAll(userlist);
                        TheStage.listView.setItems(TheStage.data);
                });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            /**
             * 群聊的消息
             */
            if(head.startsWith("messageall"))
            {
                String message;
                try {
                    message = new String(AESCoder.decrypt(content,head.split("//")[1]));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String username = "用户//+temp/"+msg.sender();
                for(String user:userlist)
                {
                    if(user.contains(String.valueOf(msg.sender().getAddress()))&&user.contains(String.valueOf(msg.sender().getPort())))
                    {
                        username = user;
                        break;
                    }
                }
                SimpleDateFormat sdfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date timed = new Date(); // 获取当前时间
                String formattime = sdfd.format(timed);// 格式化时间
                String userinf = Objects.requireNonNull(username).split("//")[2] + ":" + formattime;
                Platform.runLater(() -> {
                    getdata.add(userinf);
                    printmessageone(message, getdata, getlist);
                });
            }
            /**
             * 私聊的消息
             */
            if(head.startsWith("messageone"))
            {
                String message;
                try {
                    message = new String(AESCoder.decrypt(content,head.split("//")[2]));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String username = "用户//+temp/"+msg.sender();
                for(String user:userlist)
                {
                    if(user.contains(String.valueOf(msg.sender().getAddress()))&&user.contains(String.valueOf(msg.sender().getPort())))
                    {
                        username = user;
                    }
                }
                if (!titlelsit.contains(username)) {
                    String finalUsername = username;
                    Platform.runLater(() -> {
                        TheStage.chatone(finalUsername +"//"+head.split("//")[1],message);
                        TheStage.titlelsit.add(finalUsername);
                    });
                }
                else {
                    solomessage.put(username+"//"+head.split("//")[1],message);
                }
            }
            if(head.startsWith("messageget"))
            {
                solomessageget.add(head.split("//")[1]);
            }
            if(head.startsWith("file")) {
                System.out.println(head);
                byte[] partfile;
                try {
                    partfile = AESCoder.decrypt(content, head.split("//")[6]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String username = "用户//+temp/"+msg.sender();
                for (String user : userlist) {
                    if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                        username = user;
                    }
                }
                try {
                    getnewfile(username.split("//")[2], filetopart, head,partfile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(head.startsWith("fsendover"))
            {
                String[] inf = head.split("//");
                String username = "用户//+temp/"+msg.sender();
                for (String user : userlist) {
                    if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                        username = user;
                    }
                }
                if(filetopart.get(username.split("//")[2]+"//"+head.split("//")[2]).size()==0)
                {
                    /**
                     * 重命名文件
                     */
                    File oldfile = new File("Receive"+"\\"+username.split("//")[2]+"\\"+head.split("//")[1]+"\\"+head.split("//")[2]+"tmp");
                    File newfile = new File("Receive"+"\\"+username.split("//")[2]+"\\"+head.split("//")[1]+"\\"+System.currentTimeMillis()+head.split("//")[2]);
                    oldfile.renameTo(newfile);
                }
                else {
                    /**
                     * 发送丢失的块.
                     */
                    String[] address = new String[2];
                    address[0] = String.valueOf(msg.sender().getAddress()).replace("/","");
                    address[1] = String.valueOf(msg.sender().getPort());
                    CopyOnWriteArrayList<String> temp=filetopart.get(username.split("//")[2]+"//"+head.split("//")[2]);
                    for(String s:temp)
                    {
                        sendLOST(address,inf[2],s,inf[1]);
                        break;
                    }
                }
            }
            if(head.startsWith("fsendlost"))
            {
                /**
                 * 让我方发送丢失块
                 */
                String[] infs = head.split("//");
                String username = "用户//+temp/"+msg.sender();
                for (String user : userlist) {
                    if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                        username = user;
                    }
                }
                try {
                    sendlostfiles(username.split("//"),new File(thefilepath.get(infs[2])),thefilepath,infs[1], Integer.valueOf(infs[3]));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                sendFF(username.split("//"),head.split("//")[1]+"//"+head.split("//")[2]);
            }
            if(head.startsWith("flost"))
            {
                byte[] partfile;
                try {
                    partfile = AESCoder.decrypt(content, head.split("//")[6]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String username = "用户//+temp/"+msg.sender();
                for (String user : userlist) {
                    if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                        username = user;
                    }
                }
                try {
                    insertContent(partfile,filetopart,head,username.split("//")[2]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(head.startsWith("delay"))
            {
                String username = "用户//+temp/"+msg.sender();
                for (String user : userlist) {
                    if (user.contains(String.valueOf(msg.sender().getAddress())) && user.contains(String.valueOf(msg.sender().getPort()))) {
                        username = user;
                    }
                }
                if(!userdelay.contains(username.split("//")[2])) {
                    userdelay.add(username.split("//")[2]);
                }
            }
        }
    }
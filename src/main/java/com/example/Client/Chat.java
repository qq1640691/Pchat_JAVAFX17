package com.example.Client;

import com.example.Code.AES;
import com.example.Code.SHA;
import com.example.GUI.Stage;
import com.example.GUI.reguler;
import com.example.fileoperate.showthefileinf;
import com.example.fileoperate.writefile;
import com.example.natserver.ping;
import com.example.thesendinf.sendinf;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.GUI.Stage.infarea;


/***
 * 文件重传要用复杂一点的网络环境做测试
 */
public class Chat {
    /**
     * 发送自身用户信息的代码
     */
    public static String myinf()
    {
        for(String str: Stage.userlist)
        {
            String id = str.split("//")[2];
            if(Objects.equals(id, Stage.ID))
            {
                return str;
            }
        }
        return null;
    }
    /**
     * 判断接收到的数据包类型
     */
    public synchronized static void dealpacketall(byte[] result, CopyOnWriteArrayList<String> userlist, DatagramSocket Client, ObservableList<Object> getdata) {
        //这里的第一句话应该是解压,
        String type = new String(result,0,4);
        switch (type)
        {
            case "list"://传入的是用户列表
                String get = new String(result,4,result.length-4);
                String thelist =get.replace("#","");
                /*
                  把用户上一次发送在线的时间给标记,如果没有收到这个标记,就gg.
                 */
                if (!userlist.contains(thelist)) {
                    Stage.userzx.put(thelist,System.currentTimeMillis());
                    userlist.add(thelist);
                }
                else {
                    Stage.userzx.put(thelist,System.currentTimeMillis());
                }
                Platform.runLater(() -> {
                    Stage.listView.getItems().clear();
                    Stage.data.addAll(userlist);
                    Stage.listView.setItems(Stage.data);
                });
                break;
            case "errr":
                Platform.runLater(() -> {
                    javafx.stage.Stage aleat = new javafx.stage.Stage();
                    Text err = new Text();
                    err.setText("用户名重复,请重新输入");
                    err.setTextAlignment(TextAlignment.CENTER);
                    err.setFill(Color.RED);
                    err.setFont(Font.font(null, FontWeight.BOLD,18));
                    Stage.POP(aleat, err);
                    Stage.all.close();
                    aleat.setOnCloseRequest(e-> System.exit(0));
                });
                break;
            case "mesl":
                String getd = new String(result,0,256);
                String[] infd = getd.split("//");
                SimpleDateFormat sdfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date timed = new Date(); // 获取当前时间
                String formattime = sdfd.format(timed);// 格式化时间
                String userinf = infd[3]+":"+formattime;
                String message = new String(result,256,result.length-256);
                Platform.runLater(()->{
                    getdata.add(userinf);
                    print(getdata, message, Stage.getlist);
                    reguler.method3("allmessage\\allmessage.txt", Stage.ID + ":" + formattime+"\n"+message);
                });
                break;
            case "meso"://单人消息
                if(Stage.titlelsit.size()==0)//没有窗口,一定要新建
                {
                    doarea(result, Client);
                }
                else {//有窗口了
                    String[] gettitle = new String(result,0,256).split("//");
                    String thekey = "/"+gettitle[1]+"//"+gettitle[2]+"//"+gettitle[3];
                    if(!Stage.titlelsit.contains(thekey))
                    {
                        doarea(result, Client);
                    }
                }
                break;
        }
    }

    public static void print(ObservableList<Object> getdata, String message, ListView<Object> getlist) {
        printmessage(getdata, message, getlist);
    }

    public static void printmessage(ObservableList<Object> getdata, String message, ListView<Object> getlist) {
        TextArea print = new TextArea();
        print.setText(message);
        print.setWrapText(true);
        print.setEditable(false);
        print.setPrefSize(500,100);
        print.setStyle("-fx-font-size: 18 ;-fx-font-weight:bold");
        getdata.add(print);
        getlist.setItems(getdata);
    }

    public static void doarea(byte[] result, DatagramSocket Client) {
        String[] creattitle = new String(result,0,256).split("//");
        String newtitle = "/"+creattitle[1]+"//"+creattitle[2]+"//"+creattitle[3];
        Platform.runLater(() -> {
            Stage.chatone(Client,newtitle,result);
            Stage.titlelsit.add(newtitle);
            Stage.allbyte.remove(result);
        });
    }

    public synchronized static void dealpacketone(DatagramSocket Client,byte[] result, String title, ObservableList<Object> getdata, ListView<Object> getlist,CopyOnWriteArrayList<String> mess) {
        //这里的第一句话应该是解压,
        String type = new String(result,0,4);
        if (type.equals("meso")) {//单人消息//有窗口了
            String[] name = title.split("//");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date time = new Date(); // 获取当前时间
            String format = sdf.format(time);// 格式化时间
            Platform.runLater(() ->
            {
                getdata.add(name[2] + ":" + format);
                TextArea print = new TextArea();
                print.setText(new String(result, 256, result.length - 256));
                print.setWrapText(true);
                print.setEditable(false);
                print.setPrefSize(500, 100);
                print.setStyle("-fx-font-size: 18 ;-fx-font-weight:bold");
                getdata.add(print);
                getlist.setItems(getdata);
                getstring(Client, result);
                reguler.method3("message\\" + name[2] + "\\" + "allmessage.txt", Stage.ID + ":" + format + "\n" + new String(result, 256, result.length - 256));
            });
        }
        if(type.equals("megt")){
            String SHA = new String(result,256,result.length-256);
            mess.removeIf(s -> com.example.Code.SHA.getResult(s).equals(SHA));
        }
    }

    public static void getstring(DatagramSocket Client, byte[] result) {
        String inf = new String(result, 0, 256);
        SocketAddress address = new InetSocketAddress(inf.split("//")[1], Integer.parseInt(inf.split("//")[2]));
        sendinf.sendmget(address, Client, SHA.getResult(new String(result, 256, result.length - 256)), myinf());
    }

    public synchronized static void dealpacketfile(byte[] result, DatagramSocket Client, ConcurrentHashMap<String, String> thefilepath) throws Exception {
        //这里的第一句话应该是解压,
        String type = new String(result, 0, 4);
        switch (type) {
            case "file":
                String[] inf1 = getfilename(result);
                if (!Stage.fileinf.containsKey(inf1[4])) {
                    CopyOnWriteArrayList<String> temp = new CopyOnWriteArrayList<>();
                    for (int k = 0; k < Integer.parseInt(inf1[6]); k++) {
                        temp.add(String.valueOf(k));
                    }
                    Stage.filetopart.put(inf1[4], temp);
                    writefile.getnewfile(result, Stage.filetopart,inf1[3]);
                    Stage.fileinf.put(inf1[4], new String(result, 0, 256) + "//" + System.currentTimeMillis());
                } else {
                    writefile.getnewfile(result, Stage.filetopart,inf1[3]);
                }
                break;
            case "fifh"://文件接收完成
                String inf = new String(result, 0, result.length);
                String[] infs = inf.split("//");
                    if (Stage.filetopart.containsKey(infs[4])&& Stage.filetopart.get(infs[4]).size() == 0) {
                        if (infs[4].contains("fzimage")) {
                            File newfile = new File("image\\"+infs[3]+"\\"+infs[4]);
                            File oldfile = new File("image\\"+infs[3]+"\\"+infs[4]+"fzt");
                            sendinf.sendfileover(renamefile(infs, newfile, oldfile),Client,infs[4],myinf(),"fzimage",infs);
                        }
                        if (infs[4].contains("fzvoice")) {
                            File newfile = new File("voice\\"+infs[3]+"\\"+infs[4]);
                            File oldfile = new File("voice\\"+infs[3]+"\\"+infs[4]+"fzt");
                            sendinf.sendfileover(renamefile(infs, newfile, oldfile),Client,infs[4],myinf(),"fzvoice",infs);
                        }
                        if (!infs[4].contains("fzvoice") && !infs[4].contains("fzimage")) {
                            File newfile = new File("file\\"+infs[3]+"\\"+infs[4]);
                            File oldfile = new File("file\\"+infs[3]+"\\"+infs[4]+"fzt");
                            sendinf.sendfileover(renamefile(infs, newfile, oldfile),Client,infs[4],myinf(),"",infs);
                        }
                    } else if(Stage.filetopart.size()>0) {
                        getlostfile(getfilename(result), Client);
                    }
                    break;
                    case "fire"://文件重传.
                        fileretran(result, Client);
                        break;
                    case "firt":
                        String inft = new String(result, 0, result.length);
                        String[] infst = inft.split("//");
                        writefile.insertContent(result, Stage.filetopart,infst[3]);
                        break;
                    case "fiov":
                        Stage.filetemp.remove(new String(result,256,result.length-256));
                        break;
                    case "fils":
                        sendover(result,Client,thefilepath);
                        break;
                }
        }

    public static SocketAddress renamefile(String[] infs, File newfile, File oldfile) {
        if(oldfile.renameTo(newfile))
        {
            infarea.appendText("文件接收完成\n");
        }
        Stage.fileinf.remove(infs[4]);
        Stage.filetopart.remove(infs[4]);
        String theip = infs[1].replace("/","");
        return new InetSocketAddress(theip, Integer.parseInt(infs[2]));
    }


    public static String[] getfilename(byte[] result)
    {
        String get;
        if (result.length>=256)
        {
            get = new String(result, 0, 256);
        }
        else {
            get = new String(result, 0, result.length);
        }
        return get.split("//");
    }
    public static synchronized void getlostfile(String[] inf,DatagramSocket client) throws Exception {
        int delay = Integer.parseInt(ping.sendthedelay(inf[1].replace("/","")));
        if(Stage.filetopart.get(inf[4]).size()==0)
        {
            sendfinish(inf, client);
        }
        else {
            for (String part : Stage.filetopart.get(inf[4])) {
                String sendbuf = "fire/" + myinf() + "//" + inf[4] + "//" + part + "//";
                if (sendbuf.getBytes(StandardCharsets.UTF_8).length < 256) {
                    sendbuf = sendbuf + "*";
                }
                sendfilefinish(inf, client, sendbuf);
                Thread.sleep(delay);
            }
            sendfinish(inf, client);
        }
    }

    public static void sendfilefinish(String[] inf, DatagramSocket client, String sendbuf) throws Exception {
        byte[] thesend = AES.encrypt(sendbuf.getBytes(StandardCharsets.UTF_8), Stage.KEY);
        SocketAddress address = new InetSocketAddress(inf[1], Integer.parseInt(inf[2]));
        DatagramPacket packet2 = new DatagramPacket(Objects.requireNonNull(thesend), thesend.length, address);
        client.send(packet2);
    }

    public static void sendfinish(String[] inf, DatagramSocket client) throws Exception {
        String sendbuf = "fils/"+myinf()+"//"+inf[4];
        sendfilefinish(inf, client, sendbuf);
    }

    /**
     * 计算用户之间的延迟,储存到hashmap
     */
    public static synchronized void receivedelay(byte[] result)
    {
        String get = new String(result,0,result.length);
        String[] inf=get.split("//");
        if(!Stage.userdelay.contains(inf[3]))
        {
            Stage.userdelay.add(inf[3]);
        }
    }
    /**
     * 接收对方索要的缺失文件块的信息,发送给对方.少写了file
     */
    public static void fileretran(byte[] resule, DatagramSocket Client) throws Exception {
//        long opentime = System.currentTimeMillis();
        String partinf = new String(resule,0,resule.length);
        String[] inf = partinf.split("//");
        String part = inf[5];//块数
        String filepath = null;
        if(inf[4].contains("fzimage"))
        {
            filepath = inf[4].replace(Stage.ID,"").replace("fzimage","");
        }
        if(inf[4].contains("fzvoice")) {
            filepath = inf[4].replace(Stage.ID, "").replace("fzvoice", "");
        }
        if(!inf[4].contains("fzimage")&&!inf[4].contains("fzvoice"))
        {
            filepath = inf[4].replace(Stage.ID,"");
        }
        byte[] file = Stage.filetemp.get(filepath);
        byte[] partfile;
        if(Integer.parseInt(inf[5])==(Objects.requireNonNull(file).length/ Stage.BYTELENGTH))
        {
            partfile = new byte[file.length% Stage.BYTELENGTH];
        }
        else {
            partfile = new byte[Stage.BYTELENGTH];
        }
        System.arraycopy(file,Integer.parseInt(part)* Stage.BYTELENGTH,partfile,0,partfile.length);
        /*
        file//127.0.0.1//58589//方正//filename//file.length//file.allpart//part
         */
        String filelength = String.valueOf(file.length/ Stage.BYTELENGTH+1);
        StringBuilder head = new StringBuilder("firt/" + myinf() + "//" + inf[4] + "//" + file.length + "//" + filelength + "//" + inf[5] + "//");
        if (head.toString().getBytes(StandardCharsets.UTF_8).length<256)
        {
            int lostlength = 256- head.toString().getBytes(StandardCharsets.UTF_8).length;
            head.append("*".repeat(Math.max(0, lostlength)));
        }
        byte[] sendbuf = new byte[partfile.length+256];
        System.arraycopy(head.toString().getBytes(StandardCharsets.UTF_8),0,sendbuf,0,256);
        System.arraycopy(partfile,0,sendbuf,256,partfile.length);
        sendbuf = AES.encrypt(sendbuf, Stage.KEY);
        SocketAddress address = new InetSocketAddress(inf[1],Integer.parseInt(inf[2]));
        DatagramPacket packet = new DatagramPacket(Objects.requireNonNull(sendbuf),sendbuf.length,address);
        try {
            Client.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendover(byte[] resule, DatagramSocket Client,ConcurrentHashMap<String, String> thefilepath)
    {
        String partinf = new String(resule,0,resule.length);
        String[] inf = partinf.split("//");
        SocketAddress address = new InetSocketAddress(inf[1],Integer.parseInt(inf[2]));
        try {
            if(inf[4].contains("fzimage"))
            {
                sendinf.sendFF(address,Client, showthefileinf.filetheinf(thefilepath.get(inf[4].replace(Stage.ID,"").replace("fzimage","")),"fzimage"), Objects.requireNonNull(myinf()),35);
            }
            if(inf[4].contains("fzvoice"))
            {
                sendinf.sendFF(address,Client, showthefileinf.filetheinf(thefilepath.get(inf[4].replace(Stage.ID,"").replace("fzvoice","")),"fzvoice"), Objects.requireNonNull(myinf()),35);
            }
            if(!inf[4].contains("fzimage")&&!inf[4].contains("fzvoice"))
            {
                sendinf.sendFF(address,Client, showthefileinf.filetheinf(thefilepath.get(inf[4].replace(Stage.ID,"")),""), Objects.requireNonNull(myinf()),35);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}


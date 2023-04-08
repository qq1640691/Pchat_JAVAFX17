package com.example.Tools;

import com.example.GUI.TheStage;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.GUI.TheStage.Popbox;

public class Instrument {
    public static boolean isIPAdress(String str) {
        Pattern pattern = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(str).matches();
    }

    public static boolean isport(String str) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(str);
        boolean result = matcher.matches();
        if (result)
        {
            int port = Integer.parseInt(str);
            return port >= 1 && port <= 65535;
        }
        else {
            return false;
        }
    }

    public static String myinf()
    {
        for(String str: TheStage.userlist)
        {
            String id = str.split("//")[2];
            if(Objects.equals(id, TheStage.ID))
            {
                return str;
            }
        }
        return null;
    }

    public static String newkey()
    {
        Random random = new Random();
        StringBuilder number = new StringBuilder();
        for (int i=0;i<16;i++) {
            int temp = random.nextInt(10);
            number.append(temp);
        }
        return String.valueOf(number);
    }

    public static String getCpuId() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        return hal.getComputerSystem().getSerialNumber();
    }

    public static String sendthedelay(String ip) throws Exception {
        BufferedReader br = null;
        try{
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("ping " + ip);
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), "GB2312");
            br = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if(!sb.toString().contains("平均")){
                return "无网络";
            }
            else{
                return sb.substring(sb.toString().lastIndexOf("平均")+5,sb.length()).replace("ms","");
            }
        }catch (Exception e){
            throw new Exception();
        }finally {
            if (br != null){
                br.close();
            }
        }
    }

    public static boolean Download(String theurl){
        try {
        //新建URL对象
            URL url=new URL(theurl);
            //文件对象，注意，并不是在本地创建一个文件，仅仅是一个文件对象
            File file=new File("keywords");
            //开启连接
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
            //UA是帮助网站识别当前访问的浏览器
            httpURLConnection.setRequestProperty("User-Agent","NetFox");
            //获得输入流
             InputStream inputStream=httpURLConnection.getInputStream();
             //文件输出流
            FileOutputStream outputStream=new FileOutputStream(file);
            byte[] b=new byte[1024];
            int nRead;
            while((nRead=inputStream.read(b,0,1024))>0){
                outputStream.write(b,0,nRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e){
            return false;
        }
        return true;
    }

    public static BufferedImage IMAGEZIP(BufferedImage inputImage, float scale) {
        //压缩之后的长度和宽度
        int outputHeight = (int)(inputImage.getHeight()*scale);
        int outputWidth = (int)(inputImage.getWidth()*scale);
        BufferedImage outputImage = new BufferedImage(outputWidth, outputHeight, inputImage.getType());
        outputImage.getGraphics().drawImage(
                inputImage.getScaledInstance(outputWidth, outputHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        return outputImage;
    }

    public static void thealert(String inf,int flag) {
        Platform.runLater(()->{
            Stage aleat = new Stage();
            Text err = new Text();
            err.setText(inf);
            err.setTextAlignment(TextAlignment.CENTER);
            err.setFill(Color.RED);
            err.setFont(Font.font(null, FontWeight.BOLD, 15));
            Popbox(aleat, err);
            if(flag==0) {
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.exit(0);
                }).start();
            }
        });

    }
}

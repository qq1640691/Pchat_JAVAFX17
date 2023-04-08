package com.example.ClientStatic;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.ClientStatic.InfOperation.Sendtheinf;

public class FileOperation {
    /**
     * 把一整段数据按照1024字节分段发送,文件地址同样可以改成字节数组,这样更方便
     */
    public static int SENDBUFSIZE = 1700;
    public static void sendbigfiles(String[] address,File thefile, ConcurrentHashMap<String,String> path,int delay,String type) throws Exception
    //需要考虑如何定义消息类型,用前256个字节定义,第一位数据包类型,第二位文件名,第三位文件分块,第四位当前块数.第五位,填充0
    {
        //有点小问题,优化一下
        String filename=thefile.getName();
        RandomAccessFile randomAccessFile=new RandomAccessFile(thefile,"r");
        path.put(filename, String.valueOf(thefile));
        long allparts = (thefile.length()/(SENDBUFSIZE))+1;
        byte[] sendbuf = new byte[SENDBUFSIZE];
        byte[] lastbuf = new byte[Math.toIntExact(thefile.length() % SENDBUFSIZE)];
        for(int i=0;i<allparts;i++)
        {
            if(i<allparts-1)//前面的文件块
            {
                /**
                 * 文件类型,文件名称,文件大小,文件总分块,文件当前块数?
                 */
                String head = "file//"+type+"//"+filename+"//"+thefile.length()+"//"+allparts+"//"+i;
                randomAccessFile.seek((long) i *SENDBUFSIZE);
                randomAccessFile.read(sendbuf,0,SENDBUFSIZE);
                Sendtheinf(address,head,sendbuf);
                Thread.sleep(delay);
            }
            if(i>=allparts-1)//最后一块内容
            {
                String head = "file//"+type+"//"+filename+"//"+thefile.length()+"//"+allparts+"//"+i;
                randomAccessFile.seek((long) i *SENDBUFSIZE);
                randomAccessFile.read(lastbuf,0,lastbuf.length);
                Sendtheinf(address,head,lastbuf);
                Thread.sleep(delay);
            }
        }
    }
    public static void sendlostfiles(String[] address,File thefile, ConcurrentHashMap<String,String> path,String type,Integer part) throws Exception
    //需要考虑如何定义消息类型,用前256个字节定义,第一位数据包类型,第二位文件名,第三位文件分块,第四位当前块数.第五位,填充0
    {
        //有点小问题,优化一下
        String filename = thefile.getName();
        RandomAccessFile randomAccessFile = new RandomAccessFile(thefile, "r");
        path.put(filename, String.valueOf(thefile));
        long allparts = (thefile.length() / (SENDBUFSIZE)) + 1;
        byte[] sendbuf = new byte[SENDBUFSIZE];
        byte[] lastbuf = new byte[Math.toIntExact(thefile.length() % SENDBUFSIZE)];
        String head = "flost//" + type + "//" + filename + "//" + thefile.length() + "//" + allparts + "//" + part;
        randomAccessFile.seek((long) part *SENDBUFSIZE);
        if (part == allparts - 1) {
            /**
             * file//image//filename//125855//555//1
             */
            randomAccessFile.read(lastbuf, 0, lastbuf.length);
            Sendtheinf(address, head, lastbuf);
        } else {
            randomAccessFile.read(sendbuf, 0,SENDBUFSIZE);
            Sendtheinf(address, head, sendbuf);
        }
    }
    public static void sendFF(String[] address,String filename) {
        String head = "fsendover//"+filename;
        Sendtheinf(address,head,null);
    }
    public static void sendLOST(String[] address,String filename,String lostpart,String type)
    {
        String head = "fsendlost//"+type+"//"+filename+"//"+lostpart;
        Sendtheinf(address,head,null);
    }

    public static void insertContent(byte[] content, ConcurrentHashMap<String, CopyOnWriteArrayList<String>> filehm, String head, String username) throws IOException {
        String[] infs = head.split("//");
        File file = new File("Receive\\"+username+"\\"+infs[1]+"\\"+infs[2]+"tmp");
        int index = Integer.parseInt(infs[5])*1700;
        writelostbyte(infs, file);
        File tmpfile = new File("temp\\"+file.getName());
        try (FileOutputStream tmpout = new FileOutputStream(tmpfile); FileInputStream tmpinput = new FileInputStream(tmpfile); RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            randomAccessFile.seek(index + 1700);
            //把该位置文件读取出来
            byte[] bytes = new byte[1700];//
            int len;
            while ((len = randomAccessFile.read(bytes)) != -1) {
                tmpout.write(bytes, 0, len);
            }
            tmpout.flush();
            //将指针移动到指定位置
            randomAccessFile.seek(index);
            randomAccessFile.write(content);
            //将临时文件内容重写写入原文件
            while ((len = tmpinput.read(bytes)) != -1) {
                randomAccessFile.write(bytes, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        CopyOnWriteArrayList<String> tempd = filehm.get(username+"//"+infs[2]);
        tempd.remove(infs[5]);
        filehm.put(username+"//"+infs[2],tempd);
    }

    static void writelostbyte(String[] infs, File file) throws IOException {
        if(!file.exists()){
            RandomAccessFile r = null;
            try {
                r = new RandomAccessFile(file, "rw");
                r.setLength(Integer.parseInt(infs[3]));
            } finally{
                if (r != null) {
                    try {
                    r.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void getnewfile(String username, ConcurrentHashMap<String,CopyOnWriteArrayList<String>> filehm,String head,byte[] content) throws IOException {
        /**
         * file//file//419109070122方正开题报告.docx//112105//66//62//6327668209275192
         */
        String[] infs = head.split("//");
        CopyOnWriteArrayList<String> tempd = new CopyOnWriteArrayList<>();
        if(!filehm.containsKey(username+"//"+infs[2]))
        {
            for(int i=0;i<Integer.parseInt(infs[4]);i++)
            {
                tempd.add(String.valueOf(i));
            }
            filehm.put(username+"//"+infs[2],tempd);
        }
        else {
            tempd = filehm.get(username+"//"+infs[2]);
        }
        if (!(Integer.parseInt(infs[5])<Integer.parseInt(tempd.get(0))))
        {
            File file = new File("Receive\\"+username+"\\"+infs[1]+"\\"+infs[2]+"tmp");
            writebyte(content, filehm, infs, file, tempd,username);
        }
    }

    static void writebyte(byte[] result, ConcurrentHashMap<String, CopyOnWriteArrayList<String>> filehm, String[] infs, File file, CopyOnWriteArrayList<String> tempd,String usename) throws IOException {
        int index = Integer.parseInt(infs[5])*1700;
        writelostbyte(infs, file);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            randomAccessFile.seek(index);
            randomAccessFile.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempd.remove(infs[5]);
        filehm.put(usename+"//"+infs[2],tempd);
    }
}


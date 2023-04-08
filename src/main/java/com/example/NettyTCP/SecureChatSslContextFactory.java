package com.example.NettyTCP;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Objects;

public final class SecureChatSslContextFactory {
    private static final String PROTOCOL = "TLSv1.2";
    private static SSLContext CLIENT_CONTEXT;//客户端安全套接字协议

    public static SSLContext getClientContext(String caPath){
        if(CLIENT_CONTEXT!=null) return CLIENT_CONTEXT;
        InputStream tIN = null;
        try{
            //信任库
            TrustManagerFactory tf = null;
            if (caPath != null) {
                //密钥库KeyStore
                KeyStore tks = KeyStore.getInstance("JKS");
                //加载客户端证书
                tIN = new FileInputStream(caPath);
                tks.load(null);
                //生成验证工厂
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                //生成别名(可以随便填写)
                String certificateAlias = Integer.toString(0);
                tks.setCertificateEntry(certificateAlias,certificateFactory.generateCertificate(tIN));
                tf = TrustManagerFactory.getInstance("SunX509");
                // 初始化信任库
                tf.init(tks);
            }
            CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
            //设置信任证书
            CLIENT_CONTEXT.init(null, Objects.requireNonNull(tf).getTrustManagers(), null);
        }catch(Exception e){
            e.printStackTrace();
            throw new Error("Failed to initialize the client-side SSLContext");
        }finally{
            if(tIN !=null){
                try {
                    tIN.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return CLIENT_CONTEXT;
    }
}
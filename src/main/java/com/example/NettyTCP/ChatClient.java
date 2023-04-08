package com.example.NettyTCP;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;


/**
 * @author zwz
 */
public class ChatClient {
    public static void nettyclient(byte[] send,String IP,int PORT) throws InterruptedException {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup);
        bootstrap.channel(NioSocketChannel.class);
        SSLEngine engine = SecureChatSslContextFactory.getClientContext("server.cer").createSSLEngine();//创建SSLEngine
        engine.setUseClientMode(true);//客户方模式
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                ChannelPipeline chhanl=ch.pipeline();
                // 将字符串编解码器及客户端处理器添加到pipeline中
                SSLEngine engine = SecureChatSslContextFactory.getClientContext("server.cer").createSSLEngine();//创建SSLEngine
                engine.setUseClientMode(true);//客户方模式
                chhanl.addLast("ssl", new SslHandler(engine));
                chhanl.addLast(new ChatClientHandler());
            }
        });
        // 连接服务端
         ChannelFuture channelFuture = bootstrap.connect(IP, PORT);
        channelFuture.sync();
        ByteBuf rs = Unpooled.buffer(4096);
        rs.writeBytes(send);
        channelFuture.channel().writeAndFlush(rs);
    }
}

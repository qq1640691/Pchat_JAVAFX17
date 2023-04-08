package com.example.NettyTCP;

import com.example.Start;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import static com.example.GUI.TheStage.Popbox;

/**
 * @author zwz
 */

public class ChatClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String get = new String(ByteBufUtil.getBytes((ByteBuf) msg));
        switch (get) {
            case "OK" -> Start.RECEIVE = "OK";
            case "NO" -> Start.RECEIVE = "NO";
            case "RECEIVE" -> Platform.runLater(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Stage aleat = new Stage();
                Text err = new Text();
                err.setText("举报成功");
                err.setTextAlignment(TextAlignment.CENTER);
                err.setFill(Color.RED);
                err.setFont(Font.font(null, FontWeight.BOLD, 20));
                Popbox(aleat, err);
            });
        }
        ctx.close();
    }
}

package com.nhjclxc.tcpclient.config;

import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TCPClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("Receive TCPServer Message:\n" + msg);

        JSONObject dataJsonObject = JSONObject.parseObject(msg);
        String cmd = dataJsonObject.getString("cmd");
        String id = dataJsonObject.getString("id");


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("TCPClient Error", cause);
        ctx.close();
    }
}

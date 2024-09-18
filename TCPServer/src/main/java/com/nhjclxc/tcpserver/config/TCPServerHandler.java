package com.nhjclxc.tcpserver.config;

import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Component
public class TCPServerHandler extends SimpleChannelInboundHandler<String> {

/**
*
    连接TCP服务器时客户端发送的消息格式为：{"id":"1","cmd":"connection"}，表示该客户端请求接口服务器
    相应的服务器响应为：{"id":"1","cmd":"approval"}，表示允许该客户端接入


    后续的消息发送格式为：{"id":"1000","dest":"9999","cmd":"request","data": "datadatadata"}
    响应格式为：{"id":"1000","cmd":"reply","data": "datadatadata"}


*/

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("TCPServerHandler.channelRead0.msg = {}", msg);

        JSONObject dataJsonObject = JSONObject.parseObject(msg);
        String cmd = dataJsonObject.getString("cmd");
        if (cmd == null || "".equals(cmd)){
            return;
        }
        if ("connection".equals(cmd)){
            // 保存信道
            Long id = dataJsonObject.getLong("id");
            ChannelHandlerPool.saveChannel(id, ctx.channel());
            dataJsonObject.put("cmd", "approval");
            ctx.writeAndFlush(dataJsonObject.toString());
        } else if ("request".equals(cmd)){
            // 消息处理与转发
            Long id = dataJsonObject.getLong("id");
            Long dest = dataJsonObject.getLong("dest");
            String data = dataJsonObject.getString("data");
            Channel channel = ChannelHandlerPool.getChannel(dest);
            assert channel != null;
            channel.writeAndFlush(data);
        } else {
            Object res = "接收数据完成！！！" + System.currentTimeMillis();
            ctx.writeAndFlush(res);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("TCPServer出现异常", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

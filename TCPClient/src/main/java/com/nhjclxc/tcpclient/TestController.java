package com.nhjclxc.tcpclient;

import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.ChannelFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/client")
public class TestController {


    @Autowired
    private ChannelFuture channelFuture;

    /**
     *
     连接TCP服务器时客户端发送的消息格式为：{"id":"1","cmd":"connection"}，表示该客户端请求接口服务器
     相应的服务器响应为：{"id":"1","cmd":"approval"}，表示允许该客户端接入


     后续的消息发送格式为：{"id":"1000","cmd":"request","data": "datadatadata"}
     响应格式为：{"id":"1000","cmd":"reply","data": "datadatadata"}


     */
    @GetMapping("/connect")
    public Object connect(Long clientId) {
        JSONObject msg  = new JSONObject();
        msg.put("id", clientId);
        msg.put("cmd", "connection");
        channelFuture.channel().writeAndFlush(msg.toString());
        return "success";
    }

    @GetMapping("/message")
    public Object message(Long clientId, String data) {
        JSONObject msg  = new JSONObject();
        msg.put("id", clientId);
        msg.put("cmd", "request");
        msg.put("data", data);
        channelFuture.channel().writeAndFlush(msg.toString());
        return "success";
    }


}

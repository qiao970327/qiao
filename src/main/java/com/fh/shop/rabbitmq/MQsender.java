package com.fh.shop.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.fh.shop.config.MQConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 让它被SpingIOC容器管理起来
@Component
public class MQsender {

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendGoodsMessage(GoodsMessage goodsMessage){
        String goodsMessageJson = JSONObject.toJSONString(goodsMessage);
        // 第一个指明是那个交换机名，第二个指明绑定的那个交换机，最后是要发送的消息
        amqpTemplate.convertAndSend(MQConfig.GOODS_EXCHANGE,MQConfig.GOODS_ROUTE_KEY,goodsMessageJson);
    }

    public void sendMailMessage(String mailMessage){
        amqpTemplate.convertAndSend(MQConfig.GOODS_EXCHANGE,MQConfig.GOODS_ROUTE_KEY,mailMessage);
    }

}

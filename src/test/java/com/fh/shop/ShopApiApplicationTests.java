package com.fh.shop;


import com.fh.shop.rabbitmq.GoodsMessage;
import com.fh.shop.rabbitmq.MQsender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootTest
class ShopApiApplicationTests {
    @Autowired
    JavaMailSenderImpl mailSender;



    @Autowired
    private MQsender mqSender;

    @Test
    public void test1(){
        for (int i = 0; i <= 10; i++) {
            GoodsMessage goodsMessage = new GoodsMessage();
            goodsMessage.setId(Long.parseLong(i+""));
            goodsMessage.setPrice("300"+i);
            goodsMessage.setStock(10L);
            mqSender.sendGoodsMessage(goodsMessage);
        }
    }

}

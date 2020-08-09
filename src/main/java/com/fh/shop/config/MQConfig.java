package com.fh.shop.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    public static final String GOODS_EXCHANGE = "goodsExchange";
    public static final String GOODS_ROUTE_KEY = "goods";
    public static final String GOODS_QUEUE = "goods-queue";

    public static final String ORDER_EXCHANGE = "orderExchange";
    public static final String ORDER_QUEUE = "orderQueue";
    public static final String ORDER_ROUT_KEY = "order";

    @Bean
    public DirectExchange orderExchange(){
        return new DirectExchange(ORDER_EXCHANGE,true,false);
    }

    @Bean
    public Queue orderQueue(){
        return new Queue(ORDER_QUEUE,true);
    }

    @Bean
    public Binding orderBinding(){
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUT_KEY);
    }




    @Bean
    public DirectExchange goodsExchange(){
        // name="交换机名字" durable="持久化" autoDelete="是否自动删除"
        return new DirectExchange(GOODS_EXCHANGE,true,false);
    }

    @Bean
    public Queue goodsQueue(){
        return new Queue(GOODS_QUEUE,true);
    }

    @Bean
    public Binding goodsBinding(){
        return BindingBuilder.bind(goodsQueue()).to(goodsExchange()).with(GOODS_ROUTE_KEY);
    }

}

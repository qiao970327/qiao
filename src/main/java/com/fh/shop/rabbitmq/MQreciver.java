package com.fh.shop.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.cart.po.Cart;
import com.fh.shop.cart.po.CartItem;
import com.fh.shop.config.MQConfig;
import com.fh.shop.exception.StockLessException;
import com.fh.shop.order.biz.OrderService;
import com.fh.shop.order.param.OrderParam;
import com.fh.shop.product.mapper.ProductMapper;
import com.fh.shop.product.po.Product;
import com.fh.shop.utils.KeyUtil;
import com.fh.shop.utils.MailUtil;
import com.fh.shop.utils.RedisUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MQreciver {

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = MQConfig.GOODS_QUEUE)
    public void handleGoodsMessage(String mailMessage,Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        Long deliveryTag = messageProperties.getDeliveryTag();

        MailMessage mail = JSONObject.parseObject(mailMessage,MailMessage.class);
        String to = mail.getTo();
        String subject = mail.getSubject();
        String content = mail.getContent();
        mailUtil.DaoMail(to,content,subject);
        channel.basicAck(deliveryTag,false);
    }

    @RabbitListener(queues = MQConfig.ORDER_QUEUE)
    public void handleOrderMessage(String msg, Message message, Channel channel) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        Long deliveryTag = messageProperties.getDeliveryTag();


        OrderParam orderParam = JSONObject.parseObject(msg, OrderParam.class);
        Long memberId = orderParam.getMemberId();
        // 获取reids中的购物车商品
        String cartJson = RedisUtil.get(KeyUtil.buildCartKey(memberId));
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        // 购买的商品数量要跟数据库中的商品库存做对比，判断库存是否充足，进行提醒
        if(cart == null){
            // 从消息队列中删除指定消息
            channel.basicAck(deliveryTag,false);
            return;
        }
        List<CartItem> cartItemList = cart.getCartItemList();
        List<Long> goodIdList = cartItemList.stream().map(x -> x.getGoodsId()).collect(Collectors.toList());
        // 根据id集合去商品表中查出对应的商品集合
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.in("id",goodIdList);
        List<Product> productList = productMapper.selectList(productQueryWrapper);
        for (CartItem item : cartItemList) {
            for (Product product : productList) {
                if(item.getGoodsId().longValue() == product.getId().longValue()){
                    if(item.getNum() > product.getStock()){
                        // 提醒库存不足
                        RedisUtil.set(KeyUtil.buildStockLessKey(memberId),"stock less");
                        // 从消息队列中删除指定消息
                        channel.basicAck(deliveryTag,false);
                        return;
                    }
                }
            }
        }

        // 创建订单
        try {
            orderService.createOrder(orderParam);
            // 从消息队列中删除指定消息
            channel.basicAck(deliveryTag,false);
        } catch (StockLessException e) {
            e.printStackTrace();
            // 提醒库存不足
            RedisUtil.set(KeyUtil.buildStockLessKey(memberId),"stock less");
            // 从消息队列中删除指定消息
            channel.basicAck(deliveryTag,false);
        } catch (Exception e){
            e.printStackTrace();
            // 提示支付日志创建失败
            RedisUtil.set(KeyUtil.buildOrderErrorKey(memberId),"error");
            // 从消息队列中删除指定消息
            channel.basicAck(deliveryTag,false);
        }
    }

}

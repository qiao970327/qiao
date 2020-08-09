package com.fh.shop.order.biz;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fh.shop.cart.po.Cart;
import com.fh.shop.cart.po.CartItem;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.common.SystemConstant;
import com.fh.shop.config.MQConfig;
import com.fh.shop.exception.StockLessException;
import com.fh.shop.order.mapper.OrderItemMapper;
import com.fh.shop.order.mapper.OrderMapper;
import com.fh.shop.order.param.OrderParam;
import com.fh.shop.order.po.Order;
import com.fh.shop.order.po.OrderItem;
import com.fh.shop.order.vo.OrderConfigVo;
import com.fh.shop.paylog.mapper.PayLogMapper;
import com.fh.shop.paylog.po.PayLog;
import com.fh.shop.product.mapper.ProductMapper;
import com.fh.shop.recipient.biz.RecipientService;
import com.fh.shop.recipient.mapper.RecipientMapper;
import com.fh.shop.recipient.po.Recipient;
import com.fh.shop.utils.KeyUtil;
import com.fh.shop.utils.RedisUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("orderService")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RecipientMapper recipientMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayLogMapper payLogMapper;


    @Override
    public ServerResponse generateOrderConfirm(Long memberId) {
        List<Recipient> recipientList = recipientService.findList(memberId);
        String cartJson = RedisUtil.get(KeyUtil.buildCartKey(memberId));
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        OrderConfigVo order = new OrderConfigVo();
        order.setCart(cart);
        order.setRecipientList(recipientList);
        return ServerResponse.success(order);
    }

    @Override
    public ServerResponse generateOrder(OrderParam orderParam) {
        // 清空Redis中的订单标志位(标识符)
        Long memberId = orderParam.getMemberId();
        RedisUtil.del(KeyUtil.buildOrderKey(memberId));
        RedisUtil.del(KeyUtil.buildStockLessKey(memberId));
        RedisUtil.del(KeyUtil.buildOrderErrorKey(memberId));
        // 将订单信息发送到消息队列中
        String orderParamJson = JSONObject.toJSONString(orderParam);
        rabbitTemplate.convertAndSend(MQConfig.ORDER_EXCHANGE,MQConfig.ORDER_ROUT_KEY,orderParamJson);
        return ServerResponse.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderParam orderParam) {
        Long memberId = orderParam.getMemberId();
        String cartJson = RedisUtil.get(KeyUtil.buildCartKey(memberId));
        Cart  cart = JSONObject.parseObject(cartJson,Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        // 减库存[数据库的乐观锁]
        // update t_product set stock=stock-num where id=productId and stock >= num
        // 考虑到并发
        for (CartItem item : cartItemList) {
            Long goodsId = item.getGoodsId();
            int num = item.getNum();
            int rowCount = productMapper.updateStock(goodsId,num);
            if (rowCount == 0){
                // 没有更新库存
                throw new StockLessException("stock less");
            }
        }
        // 获取对应的收件人
        Long  recipientId = orderParam.getRecipientId();
        Recipient recipient = recipientMapper.selectById(recipientId);
        // 插入订单表
        Order order = new Order();
        // 手工设置Id
        String orderId = IdWorker.getIdStr();
        order.setId(orderId);
        order.setCreateTime(new Date());
        order.setRecipientor(recipient.getRecipientor());
        order.setAddress(recipient.getAddress());
        order.setPhone(recipient.getPhone());
        order.setUserId(memberId);
        BigDecimal totalPrice = cart.getTotalPrice();
        order.setTotalPrice(totalPrice);
        order.setTotalNum(cart.getTotalCount());
        order.setRecipientID(recipientId);
        int payType = orderParam.getPayType();
        order.setPayType(payType);
        order.setStatus(SystemConstant.OrderStatus.WAIT_PAY);// 未支付
        orderMapper.insert(order);
        // 插入订单明细表
        // 批量插入提高性能
        List<OrderItem> orderItemList = new ArrayList<>();
        for (CartItem item : cartItemList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getGoodsId());
            orderItem.setImgUrl(item.getImage());
            orderItem.setPrice(item.getPrice());
            orderItem.setProductName(item.getGoodsName());
            orderItem.setNum(item.getNum());
            orderItem.setSubPrice(item.getSubPrice());
            orderItem.setUserId(memberId);
            orderItemList.add(orderItem);
        }
        // 批量插入订单明细表
        orderItemMapper.bathInsert(orderItemList);
        // 插入支付日志表
        PayLog payLog = new PayLog();
        String outTradeNo = IdWorker.getIdStr();
        payLog.setOutTradeNo(outTradeNo);
        payLog.setPayMoney(totalPrice);
        payLog.setUserId(memberId);
        payLog.setCreateTime(new Date());
        payLog.setOrderId(orderId);
        payLog.setPayStatus(SystemConstant.PayStatus.WAIY_PAY);
        payLog.setPayType(payType);
        payLogMapper.insert(payLog);
        // 将支付日志存入Redis
        String payLogJson = JSONObject.toJSONString(payLog);
        RedisUtil.set(KeyUtil.buildPayLogKey(memberId),payLogJson);
        // 删除购物车中的信息
        RedisUtil.del(KeyUtil.buildCartKey(memberId));
        // 提交订单成功
        RedisUtil.set(KeyUtil.buildOrderKey(memberId),"success");

    }

    @Override
    public ServerResponse getResult(Long memberId) {
        // 查看Redis中是否有订单中商品库存已不足key
        if(RedisUtil.exist(KeyUtil.buildStockLessKey(memberId))){
            // 删除订单中商品不足标志位
           RedisUtil.del(KeyUtil.buildStockLessKey(memberId));
           return ServerResponse.error(ResponseEnum.ORDER_STOCK_LESS);
        }

        // 查看Redis中是否有订单创建成功key
        if(RedisUtil.exist(KeyUtil.buildOrderKey(memberId))){
            // 删除订单创建成功标志位
           RedisUtil.del(KeyUtil.buildOrderKey(memberId));
           return ServerResponse.success();
        }

        if(RedisUtil.exist(KeyUtil.buildOrderErrorKey(memberId))){
            // 删除订单创建失败标志位
            RedisUtil.del(KeyUtil.buildOrderErrorKey(memberId));
            return ServerResponse.error(ResponseEnum.ORDER_IS_ERROR);
        }
        return ServerResponse.error(ResponseEnum.ORDER_IS_QUEUE);
    }
}

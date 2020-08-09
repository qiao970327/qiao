package com.fh.shop.product.controller;

import com.fh.shop.common.ServerResponse;
import com.fh.shop.product.biz.ProductService;
import com.fh.shop.rabbitmq.GoodsMessage;
import com.fh.shop.rabbitmq.MQsender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/products")
@Api(tags = "商品接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private MQsender mqSender;


    @GetMapping
    @ApiOperation("获取商品信息")
    public ServerResponse findList(){
        return  productService.findList();
    }

    @GetMapping("/sendMessage")
    @ApiOperation("消息中间件调试接口，发消息")
    public ServerResponse sendMessage(GoodsMessage goodsMessage){
        mqSender.sendGoodsMessage(goodsMessage);
        return ServerResponse.success();
    }

}

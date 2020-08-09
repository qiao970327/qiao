package com.fh.shop.order.controller;

import com.fh.shop.annotation.Check;
import com.fh.shop.annotation.Idempotent;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.common.SystemConstant;
import com.fh.shop.member.vo.MemberVo;
import com.fh.shop.order.biz.OrderService;
import com.fh.shop.order.param.OrderParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
@Api(tags = "订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/generateOrderConfirm")
    @Check
    @ApiOperation("生成确认订单接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",required = true,type = "string",paramType = "header")
    })
    public ServerResponse generateOrderConfirm(HttpServletRequest request){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return orderService.generateOrderConfirm(memberId);
    }


    @PostMapping("/generateOrder")
    @Idempotent
    @Check
    @ApiOperation("生成确认订单接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",required = true,type = "string",paramType = "header"),
            @ApiImplicitParam(name = "x-token",value = "token头信息",required = true,type = "string",paramType = "header"),
            @ApiImplicitParam(name = "recipientId",value = "收件人Id",required = true,type = "long",paramType = "query"),
            @ApiImplicitParam(name = "payType",value = "支付类型",required = true,type = "int",paramType = "query")
    })
    public ServerResponse generateOrder(HttpServletRequest request, OrderParam orderParam){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        orderParam.setMemberId(memberId);
        return orderService.generateOrder(orderParam);
    }


    @GetMapping("getResult")
    @Check
    @ApiOperation("查看订单状态接口")
    public ServerResponse getResult(HttpServletRequest request){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return orderService.getResult(memberId);
    }
}

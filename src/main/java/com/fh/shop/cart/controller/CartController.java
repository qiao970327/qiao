package com.fh.shop.cart.controller;

import com.fh.shop.annotation.Check;
import com.fh.shop.cart.biz.CartService;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.common.SystemConstant;
import com.fh.shop.member.vo.MemberVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/api/cart")
@RestController
@CrossOrigin
@Api(tags = "购物车接口")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("addCart")
    @Check
    @ApiOperation("添加商品到购物车")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true ,paramType = "header"),
            @ApiImplicitParam(name = "goodsId",value = "商品Id",type = "long",required = true,paramType = "query"),
            @ApiImplicitParam(name = "num",value = "商品数量",type = "int",required = true,paramType = "query")
    })
    public ServerResponse addCart(HttpServletRequest request,Long goodsId,int num){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return cartService.addCart(memberId,goodsId,num);
    }

    @GetMapping("findCart")
    @Check
    @ApiOperation("查询购物车中的商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true ,paramType = "header")
    })
    public ServerResponse findItemList(HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = memberVo.getId();
        return cartService.findItemList(memberId);
    }

    @GetMapping("getCartTotalCount")
    @Check
    @ApiOperation("获取购物车中的商品总数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true ,paramType = "header")
    })
    public ServerResponse getCartTotalCount(HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = memberVo.getId();
        return cartService.getCartTotalCount(memberId);
    }


    @PostMapping("changeCartItemCheckedStatus")
    @Check
    public ServerResponse changeCartItemCheckedStatus(Long goodsId,HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = memberVo.getId();
        return cartService.changeCartItemCheckedStatus(goodsId,memberId);
    }

    // 修改购物车中所有商品选中状态的API接口
    @PostMapping("changeAllCartItemCheckedStatus")
    @Check
    public ServerResponse changeAllCartItemCheckedStatus(Boolean checked,HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = memberVo.getId();
        return cartService.changeAllCartItemCheckedStatus(checked,memberId);
    }


    // 根据商品ID删除商品
    @PostMapping("deleteCartItem")
    @Check
    public ServerResponse deleteCartItem(Long goodsId,HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = memberVo.getId();
        return cartService.deleteCartItem(goodsId,memberId);
    }


    // 批量删除选中商品
    @PostMapping("batchDeleteCartItem")
    @Check
    public ServerResponse batchDeleteCartItem(HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = memberVo.getId();
        return cartService.batchDeleteCartItem(memberId);
    }
}

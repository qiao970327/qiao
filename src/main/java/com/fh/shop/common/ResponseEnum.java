package com.fh.shop.common;

public enum ResponseEnum {
    OK(200,"操作成功"),
    ERROR(500,"操作失败"),

    ORDER_STOCK_LESS(4000,"订单中商品,库存不足"),
    ORDER_IS_QUEUE(4001,"订单正在排队"),
    ORDER_IS_ERROR(4002,"下单失败"),

    PAY_IS_FAIL(5000,"支付失败"),

    TOKEN_IS_MISS(6000,"token头信息丢失"),
    TOKEN_IS_ERROR(6001,"token头信息错误"),
    TOKEN_REQUEST_REPET(6002,"请求重复"),


    CART_PRODUCT_IS_NULL(3000,"添加的商品不存在"),
    CART_PRODUCT_IS_DOWN(3001,"添加的商品已下架"),
    CART_NUM_IS_ERROR(3002,"商品数量格式错误"),

    LOGIN_INFO_IS_NULL(2000,"会员用户名密码为空"),
    LOGIN_MEMBER_NAME_IS_NOT_EXIT(2001,"会员不存在"),
    LOGIN_PASSWORD_IS_ERROR(2002,"用户名密码错误"),
    LOGIN_HEADER_IS_MISS(2003,"头信息丢失"),
    LOGIN_HEADER_CONTENT_IS_MISS(2004,"头信息不完整"),
    LOGIN_MEMBER_IS_CHANGE(2005,"会员信息被篡改"),
    LOGIN_TIME_OUT(2006,"会员登陆信息超时"),

    REG_MemberName_Exist(1001,"会员名称已存在"),
    REG_MemberMail_Exist(1002,"会员邮箱已存在"),
    REG_MemberPhone_Exist(1003,"会员手机号已存在"),
    REG_Member_Is_NULL(1000,"会员信息不能为空");

    private int code;
    private String msg;

    private ResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

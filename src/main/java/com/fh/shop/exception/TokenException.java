package com.fh.shop.exception;

import com.fh.shop.common.ResponseEnum;

// 自定义幂等性异常
public class TokenException extends RuntimeException {

    private ResponseEnum responseEnum;

    public TokenException(ResponseEnum responseEnum){
        this.responseEnum = responseEnum;
    }

    public ResponseEnum getResponseEnum(){
        return responseEnum;
    }

}

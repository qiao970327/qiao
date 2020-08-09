package com.fh.shop.exception;

import com.fh.shop.common.ResponseEnum;

// 自定义异常
public class GlobalException extends RuntimeException {

    private ResponseEnum  responseEnum;

    public GlobalException(ResponseEnum responseEnum){
        this.responseEnum = responseEnum;
    }

    public ResponseEnum getResponseEnum(){
        return responseEnum;
    }
}

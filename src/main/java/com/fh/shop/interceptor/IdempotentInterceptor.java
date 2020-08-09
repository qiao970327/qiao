package com.fh.shop.interceptor;

import com.fh.shop.annotation.Idempotent;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.exception.TokenException;
import com.fh.shop.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class IdempotentInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 通过自定义注解实现拦截具体方法
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if(!method.isAnnotationPresent(Idempotent.class)){
            return true;
        }
        // 获取头信息
        String tokenHeader = request.getHeader("x-token");
        if(StringUtils.isEmpty(tokenHeader)){
            throw new TokenException(ResponseEnum.TOKEN_IS_MISS);
        }
        // 判断在redis中是否存在该token
        boolean exist = RedisUtil.exist(tokenHeader);
        if(!exist){
            throw new TokenException(ResponseEnum.TOKEN_IS_ERROR);
        }
        Long del = RedisUtil.del(tokenHeader);
        if(del == 0){
            // 证明不是第一次请求
            throw new TokenException(ResponseEnum.TOKEN_REQUEST_REPET);
        }
        // 放行
        return true;
    }
}

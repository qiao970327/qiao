package com.fh.shop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.fh.shop.annotation.Check;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.SystemConstant;
import com.fh.shop.exception.GlobalException;
import com.fh.shop.member.vo.MemberVo;
import com.fh.shop.utils.KeyUtil;
import com.fh.shop.utils.Md5Util;
import com.fh.shop.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Base64;

public class LoginInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 通过自定义注解实现拦截具体方法
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if(!method.isAnnotationPresent(Check.class)){
            return true;
        }
        // ==========拦截验证============
        // 先判断是否存在头信息x-auth
        // 判断头信息是否完整
        // 判断用户信息是否被篡改[验签]
        // 判断是否超时[redis]
        // 续命[在用户信息还没超时前，给用户重新生成过期时间]
        // 给用户信息存入request方便后续使用
        // =============================
        String header = request.getHeader("x-auth");
        //获取请求头信息中的token
        if(StringUtils.isEmpty(header)){
            throw new GlobalException(ResponseEnum.LOGIN_HEADER_IS_MISS);
        }
        // 判断头信息是否完整
        String[] split = header.split("\\.");
        if(split.length != 2){
            throw new GlobalException(ResponseEnum.LOGIN_HEADER_CONTENT_IS_MISS);
        }
        // 判断用户信息是否被篡改
        String memberBase64Json = split[0];
        String signBase64 = split[1];
        // 重新生成新的签名和客户端传递过来的签名进行对比
        String newSign = Md5Util.sign(memberBase64Json, Md5Util.SECRET);
        String newSignBase64 = Base64.getEncoder().encodeToString(newSign.getBytes("UTF-8"));
        if(!signBase64.equals(newSignBase64)){
            throw new GlobalException(ResponseEnum.LOGIN_MEMBER_IS_CHANGE);
        }
        // 判断是否超时
        String memberJson = new String(Base64.getDecoder().decode(memberBase64Json),"UTF-8");
        MemberVo memberVo = JSONObject.parseObject(memberJson, MemberVo.class);
        Long id = memberVo.getId();
        String uuid = memberVo.getUuid();
        boolean exist = RedisUtil.exist(KeyUtil.buildMemberKey(uuid, id));
        if(!exist){
            throw new GlobalException(ResponseEnum.LOGIN_TIME_OUT);
        }
        // 续命
        RedisUtil.expire(KeyUtil.buildMemberKey(uuid,id),KeyUtil.MEMBER_KEY_EXPIRE);
        // 存入request中，方便后续使用
        request.setAttribute(SystemConstant.CURR_MEMBER,memberVo);
        // 放行
        return true;
    }

}

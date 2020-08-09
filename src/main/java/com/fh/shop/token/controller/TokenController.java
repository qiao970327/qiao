package com.fh.shop.token.controller;

import com.fh.shop.annotation.Check;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.token.biz.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@CrossOrigin
@RequestMapping("/api/token")
@Api(tags = "生成Token的接口")
public class TokenController {

    @Resource(name = "tokenService")
    private TokenService tokenService;

    @Check
    @PostMapping("createToken")
    @ApiOperation("生成Token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header")
    })
    public ServerResponse createToken(){
        return tokenService.createToken();
    }
}

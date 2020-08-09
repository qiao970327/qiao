package com.fh.shop.pay.controller;

import com.fh.shop.annotation.Check;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.common.SystemConstant;
import com.fh.shop.member.vo.MemberVo;
import com.fh.shop.pay.biz.PayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@RequestMapping("/api/pay")
@Api(tags = "支付接口")
public class PayController {

    @Resource(name = "payService")
    private PayService payService;

    @PostMapping("createNative")
    @ApiOperation("统一下单")
    @Check
    @ApiImplicitParams({
            @ApiImplicitParam(name="x-auth",value = "头信息",required = true,type = "string",paramType = "header")
    })
    public ServerResponse createNative(HttpServletRequest request){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return payService.createNative(memberId);
    }

    @GetMapping("queryStatus")
    @ApiOperation("查看支付状态")
    @Check
    @ApiImplicitParams({
            @ApiImplicitParam(name="x-auth",value = "头信息",required = true,type = "string",paramType = "header")
    })
    public ServerResponse queryStatus(HttpServletRequest request){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return payService.queryStatus(memberId);
    }
}

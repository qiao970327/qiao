package com.fh.shop.area.controller;

import com.fh.shop.area.biz.AreaService;
import com.fh.shop.common.ServerResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/areas")
@Api(tags = "地区接口")
public class AreaController {

    @Autowired
    private AreaService areaService;


    @GetMapping
    @ApiOperation("获取地区信息")
    // 注册方法得用到地区三级联动,不做拦截处理
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "父ID",type = "long",required = true,paramType = "query")
    })
    public ServerResponse findChrds(Long id){
        return areaService.findChrds(id);
    }

}

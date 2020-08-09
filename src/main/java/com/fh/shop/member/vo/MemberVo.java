package com.fh.shop.member.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberVo implements Serializable {

    private Long id;

    private String memberName;

    private String realName;

    private String uuid;
}

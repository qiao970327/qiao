package com.fh.shop.order.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderParam implements Serializable {

    private Long recipientId;

    private Long memberId;

    private int payType;

}

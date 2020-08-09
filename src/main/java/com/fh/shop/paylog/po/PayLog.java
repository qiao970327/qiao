package com.fh.shop.paylog.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayLog implements Serializable {

    @TableId(type = IdType.INPUT)
    private String outTradeNo;

    private Long userId;

    private String orderId;

    private Date createTime;

    private Date payTime;

    private BigDecimal payMoney;

    private int payType;

    private int payStatus;

    private String transactionId;

}

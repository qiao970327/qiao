package com.fh.shop.order.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Order implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    private Long userId;

    private Date createTime;

    private Date payTime;

    private int status;

    private BigDecimal totalPrice;

    private int totalNum;

    private int payType;

    private Long recipientID;

    private String recipientor;

    private String address;

    private String phone;
}

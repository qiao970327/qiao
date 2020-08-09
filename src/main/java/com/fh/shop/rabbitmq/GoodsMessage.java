package com.fh.shop.rabbitmq;

import lombok.Data;

import java.io.Serializable;

@Data
public class GoodsMessage implements Serializable {

    private Long id;

    private String price;

    private Long stock;
}

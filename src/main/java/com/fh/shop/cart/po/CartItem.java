package com.fh.shop.cart.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fh.shop.utils.BigDecimalSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {


    private Long goodsId; //商品id

    private String goodsName; //商品名

    @JsonSerialize(using = BigDecimalSerialize.class)
    private BigDecimal price; //商品单价

    private int num;  //商品数量

    private String image; //商品图片

    @JsonSerialize(using = BigDecimalSerialize.class)
    private BigDecimal subPrice; //商品的小计(总价)

    private Boolean checked; //记录购物车中该商品的选中状态
}

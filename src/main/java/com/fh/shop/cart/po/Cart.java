package com.fh.shop.cart.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fh.shop.utils.BigDecimalSerialize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {

    private int totalCount;//购物车中的商品总数量

    @JsonSerialize(using = BigDecimalSerialize.class)
    private BigDecimal totalPrice;//购物车中的商品的总价格

    private List<CartItem> cartItemList = new ArrayList<>();//购物车中的商品列表
}

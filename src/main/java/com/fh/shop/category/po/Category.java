package com.fh.shop.category.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_category")
public class Category {

    private Integer id;

    private String name;

    private Integer pid;




}

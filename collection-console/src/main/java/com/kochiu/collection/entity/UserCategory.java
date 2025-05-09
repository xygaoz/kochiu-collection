package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_category")
public class UserCategory extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Long cateId;
    private Integer userId;
    private String cateName;
    private Integer sno;
}

package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_tag")
public class UserTag extends BaseEntity{
    @TableId(type = IdType.INPUT)
    private Long tagId;
    private Integer userId;
    private String tagName;
}

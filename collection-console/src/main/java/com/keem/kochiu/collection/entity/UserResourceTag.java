package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_resource_tag")
public class UserResourceTag extends BaseEntity{

    @TableId(type = IdType.INPUT)
    private Long tagId;
    private Integer userId;
    private Integer resourceId;
    private String tagName;
}

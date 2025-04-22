package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_role")
public class UserRole extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Integer userRoleId;
    private Integer roleId;
    private Integer userId;
}

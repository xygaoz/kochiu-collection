package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Integer userId;
    private String userCode;
    private String userName;
    private String password;
    private Integer status;
    private String token;
    private String key;
    private String strategy;
    private LocalDateTime lastTokenTime;
}

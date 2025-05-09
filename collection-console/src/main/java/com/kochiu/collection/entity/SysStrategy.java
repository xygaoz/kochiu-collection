package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_strategy")
public class SysStrategy extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Integer strategyId;
    private String strategyCode;
    private String strategyName;
    private String serverUrl;
    private String username;
    private String password;
    private String otherConfig;
}

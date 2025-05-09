package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_module_action")
public class SysModuleAction extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Long actionId;
    private Long moduleId;
    private String actionCode;
    private String actionName;
    private String actionUrl;

    @TableField(exist = false)
    private boolean selected;
}
package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_module_action")
public class SysModuleAction extends BaseEntity {

    @TableId(value = "action_id")
    private Long actionId;

    @TableField("module_id")
    private Long moduleId;

    @TableField("action_code")
    private String actionCode;

    @TableField("action_name")
    private String actionName;

    @TableField("action_url")
    private String actionUrl;

}
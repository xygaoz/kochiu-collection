package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_permission")
public class UserPermission extends BaseEntity {

    @TableId(value = "permission_id")
    private Long permissionId;

    @TableField("role_id")
    private Long roleId;

    @TableField("module_id")
    private Long moduleId;

    @TableField("action_id")
    private Long actionId;

}
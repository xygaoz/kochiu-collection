package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_permission")
public class UserPermission extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Long permissionId;
    private Long roleId;
    private Long moduleId;
    private Long actionId;

    @TableField(exist = false)
    private String moduleName;
    @TableField(exist = false)
    private String actionName;
}
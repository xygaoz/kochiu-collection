package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_module")
public class SysModule extends BaseEntity{
    @TableId(type = IdType.AUTO)
    private Integer moduleId;
    private String moduleCode;
    private String moduleName;
    private String moduleUrl;
    private Integer parentId;
    private String icon;
    private String iconType;
    private Integer sort;
    private String style;
    private String redirect;
}
package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_config")
public class UserConfig extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Integer configId;
    private Integer userId;
    private String configKey;
    private String configValue;
    private String configDesc;
}

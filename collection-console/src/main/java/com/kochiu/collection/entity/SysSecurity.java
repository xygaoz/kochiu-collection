package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_security")
public class SysSecurity extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private Integer id;
    private String publicKey;
    private String privateKey;
    private String commonKey;
}

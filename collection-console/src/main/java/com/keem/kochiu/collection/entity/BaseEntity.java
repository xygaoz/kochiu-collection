package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.util.Date;

@Data
public abstract class BaseEntity {

    private Date createTime;
    private Date updateTime;
    private String createUser;
    private String updateUser;
    @Version
    private int dbVersion;

}

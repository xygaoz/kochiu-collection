package com.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kochiu.collection.entity.SysUser;

public interface SysUserMapper extends BaseMapper<SysUser> {

    Integer selectLastInsertId();
}





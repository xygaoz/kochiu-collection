package com.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kochiu.collection.entity.UserRole;

public interface UserRoleMapper extends BaseMapper<UserRole> {

    Integer selectLastInsertId();
}





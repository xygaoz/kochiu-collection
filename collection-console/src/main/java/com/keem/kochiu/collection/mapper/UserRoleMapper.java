package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserRole;

public interface UserRoleMapper extends BaseMapper<UserRole> {

    Integer selectLastInsertId();
}





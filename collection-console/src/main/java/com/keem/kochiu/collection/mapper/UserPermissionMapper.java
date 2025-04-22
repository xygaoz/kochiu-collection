package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserPermission;

public interface UserPermissionMapper extends BaseMapper<UserPermission> {

    Integer selectLastInsertId();
}
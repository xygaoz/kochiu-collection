package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.UserPermission;
import com.keem.kochiu.collection.mapper.UserPermissionMapper;
import org.springframework.stereotype.Service;

@Service
public class UserPermissionRepository extends ServiceImpl<UserPermissionMapper, UserPermission> {

    public boolean hasPermission(int userId, String moduleCode, String actionCode) {
        return !getBaseMapper().getUserPermission(userId, moduleCode, actionCode).isEmpty();
    }
}
package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.SysModule;
import com.kochiu.collection.entity.UserPermission;
import com.kochiu.collection.mapper.UserPermissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPermissionRepository extends ServiceImpl<UserPermissionMapper, UserPermission> {

    public boolean hasPermission(int userId, String moduleCode, String actionCode) {
        return !getBaseMapper().getUserPermission(userId, moduleCode, actionCode).isEmpty();
    }

    public List<UserPermission> getRolePermission(Long roleId) {
        return getBaseMapper().getRolePermission(roleId);
    }

    public int deleteUserPermission(Integer userId) {
        return getBaseMapper().deleteUserPermission(userId);
    }

    public List<SysModule> selectUserModule(int userId){
        return baseMapper.selectUserModule(userId);
    }

}
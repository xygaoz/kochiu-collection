package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserPermissionMapper extends BaseMapper<UserPermission> {

    Integer selectLastInsertId();

    List<UserPermission> getUserPermission(@Param("userId") int userId,
                                           @Param("moduleCode") String moduleCode,
                                           @Param("actionCode") String actionCode);

    List<UserPermission> getRolePermission(@Param("roleId") Long roleId);

    int deleteUserPermission(@Param("userId") int userId);
}
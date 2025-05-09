package com.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kochiu.collection.entity.SysModuleAction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysModuleActionMapper extends BaseMapper<SysModuleAction> {

    Integer selectLastInsertId();

    SysModuleAction selectModuleAction(@Param("moduleCode") String moduleCode,
                                             @Param("actionCode") String actionCode);

    List<SysModuleAction> getRolePermission(@Param("roleId") Integer roleId, @Param("moduleId") Integer moduleId);
}
package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.SysModuleAction;
import org.apache.ibatis.annotations.Param;

public interface SysModuleActionMapper extends BaseMapper<SysModuleAction> {

    Integer selectLastInsertId();

    SysModuleAction selectModuleAction(@Param("moduleCode") String moduleCode,
                                             @Param("actionCode") String actionCode);
}
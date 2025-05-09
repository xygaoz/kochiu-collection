package com.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kochiu.collection.entity.SysModule;

public interface SysModuleMapper extends BaseMapper<SysModule> {

    Integer selectLastInsertId();
}
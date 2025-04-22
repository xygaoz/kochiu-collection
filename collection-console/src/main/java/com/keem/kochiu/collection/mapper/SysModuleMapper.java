package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.SysModule;

public interface SysModuleMapper extends BaseMapper<SysModule> {

    Integer selectLastInsertId();
}
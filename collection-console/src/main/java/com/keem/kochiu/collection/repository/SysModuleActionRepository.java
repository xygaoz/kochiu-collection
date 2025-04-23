package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysModuleAction;
import com.keem.kochiu.collection.mapper.SysModuleActionMapper;
import org.springframework.stereotype.Service;

@Service
public class SysModuleActionRepository extends ServiceImpl<SysModuleActionMapper, SysModuleAction> {

    public SysModuleAction selectModuleAction(String moduleCode, String actionCode){
        return baseMapper.selectModuleAction(moduleCode, actionCode);
    }
}
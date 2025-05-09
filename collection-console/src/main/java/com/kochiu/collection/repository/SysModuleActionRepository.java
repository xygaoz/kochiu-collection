package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.SysModuleAction;
import com.kochiu.collection.mapper.SysModuleActionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysModuleActionRepository extends ServiceImpl<SysModuleActionMapper, SysModuleAction> {

    public SysModuleAction selectModuleAction(String moduleCode, String actionCode){
        return baseMapper.selectModuleAction(moduleCode, actionCode);
    }

    public List<SysModuleAction> getRolePermission(Integer roleId, Integer moduleId){
        return baseMapper.getRolePermission(roleId, moduleId);
    }
}
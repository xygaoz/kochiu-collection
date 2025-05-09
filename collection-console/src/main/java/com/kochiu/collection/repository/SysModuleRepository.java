package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.SysModule;
import com.kochiu.collection.mapper.SysModuleMapper;
import org.springframework.stereotype.Service;

@Service
public class SysModuleRepository extends ServiceImpl<SysModuleMapper, SysModule> {
}
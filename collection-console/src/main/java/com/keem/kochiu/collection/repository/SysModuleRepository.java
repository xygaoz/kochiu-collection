package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysModule;
import com.keem.kochiu.collection.mapper.SysModuleMapper;
import org.springframework.stereotype.Service;

@Service
public class SysModuleRepository extends ServiceImpl<SysModuleMapper, SysModule> {
}
package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysRole;
import com.keem.kochiu.collection.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRoleRepository extends ServiceImpl<SysRoleMapper, SysRole>{

    public List<SysRole> selectUserRole(int userId){
        return baseMapper.selectUserRole(userId);
    }
}

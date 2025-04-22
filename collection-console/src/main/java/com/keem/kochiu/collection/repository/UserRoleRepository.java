package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysRole;
import com.keem.kochiu.collection.entity.UserRole;
import com.keem.kochiu.collection.mapper.SysRoleMapper;
import com.keem.kochiu.collection.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class UserRoleRepository extends ServiceImpl<UserRoleMapper, UserRole>{

}

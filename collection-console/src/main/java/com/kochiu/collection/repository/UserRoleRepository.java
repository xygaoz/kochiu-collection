package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.UserRole;
import com.kochiu.collection.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class UserRoleRepository extends ServiceImpl<UserRoleMapper, UserRole>{

}

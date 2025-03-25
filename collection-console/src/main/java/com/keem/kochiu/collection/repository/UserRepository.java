package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserRepository extends ServiceImpl<UserMapper, SysUser>{

}

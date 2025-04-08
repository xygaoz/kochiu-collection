package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.UserResourceTag;
import com.keem.kochiu.collection.mapper.UserResourceTagMapper;
import org.springframework.stereotype.Service;

@Service
public class UserResourceTagRepository extends ServiceImpl<UserResourceTagMapper, UserResourceTag> {
}

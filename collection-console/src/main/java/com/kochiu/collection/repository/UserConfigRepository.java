package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.UserConfig;
import com.kochiu.collection.mapper.UserConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserConfigRepository extends ServiceImpl<UserConfigMapper, UserConfig>{

    public List<UserConfig> getUserConfig(Integer userId) {
        return list(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, userId));
    }
}

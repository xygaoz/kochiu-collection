package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.UserResourceTag;
import com.keem.kochiu.collection.mapper.UserResourceTagMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserResourceTagRepository extends ServiceImpl<UserResourceTagMapper, UserResourceTag> {

    public List<UserResourceTag> getTagList(int userId, long resourceId) {
        LambdaQueryWrapper<UserResourceTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserResourceTag::getUserId, userId);
        lambdaQueryWrapper.eq(UserResourceTag::getResourceId, resourceId);
        return baseMapper.selectList(lambdaQueryWrapper);
    }

    public boolean existsTag(int userId, long resourceId, String tagName) {
        LambdaQueryWrapper<UserResourceTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserResourceTag::getUserId, userId);
        lambdaQueryWrapper.eq(UserResourceTag::getResourceId, resourceId);
        lambdaQueryWrapper.eq(UserResourceTag::getTagName, tagName);
        return baseMapper.selectCount(lambdaQueryWrapper) > 0;
    }
}

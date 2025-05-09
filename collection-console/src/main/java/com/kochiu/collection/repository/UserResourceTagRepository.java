package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.UserResourceTag;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.mapper.UserResourceTagMapper;
import org.springframework.stereotype.Service;

@Service
public class UserResourceTagRepository extends ServiceImpl<UserResourceTagMapper, UserResourceTag> {

    /**
     * 判断标签是否存在资源里
     * @param userId
     * @param resourceId
     * @param tagId
     * @return
     */
    public boolean existsTag(int userId, Long resourceId, Long tagId){
        LambdaQueryWrapper<UserResourceTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserResourceTag::getUserId, userId);
        lambdaQueryWrapper.eq(UserResourceTag::getTagId, tagId);
        lambdaQueryWrapper.eq(UserResourceTag::getResourceId, resourceId);
        return baseMapper.selectCount(lambdaQueryWrapper) > 0;
    }

    /**
     * 添加标签
     *
     * @param userId
     * @param resourceId
     * @return
     * @throws CollectionException
     */
    public void addTag(int userId, Long resourceId, Long tagId) throws CollectionException {

        //新标签
        UserResourceTag userResourceTag = new UserResourceTag();
        userResourceTag.setResourceId(resourceId);
        userResourceTag.setTagId(tagId);
        userResourceTag.setUserId(userId);
        save(userResourceTag);
    }

    /**
     * 删除资源标签
     *
     * @param userId
     * @param tagId
     * @throws CollectionException
     */
    public void removeResourceTag(int userId, Long resourceId, Long tagId) throws CollectionException {

        LambdaQueryWrapper<UserResourceTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserResourceTag::getUserId, userId);
        lambdaQueryWrapper.eq(UserResourceTag::getTagId, tagId);
        lambdaQueryWrapper.eq(UserResourceTag::getResourceId, resourceId);
        remove(lambdaQueryWrapper);
    }

}

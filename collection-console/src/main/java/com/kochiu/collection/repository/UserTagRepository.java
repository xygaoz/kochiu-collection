package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.UserTag;
import com.kochiu.collection.enums.TagByEnum;
import com.kochiu.collection.mapper.UserTagMapper;
import com.kochiu.collection.properties.SysConfigProperties;
import com.kochiu.collection.properties.UserConfigProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTagRepository extends ServiceImpl<UserTagMapper, UserTag> {

    private final UserConfigProperties userConfigProperties;

    public UserTagRepository(UserConfigProperties userConfigProperties) {
        this.userConfigProperties = userConfigProperties;
    }

    public Long existsTag(int userId, String tagName) {
        LambdaQueryWrapper<UserTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTag::getUserId, userId);
        lambdaQueryWrapper.eq(UserTag::getTagName, tagName);
        UserTag tag = baseMapper.selectOne(lambdaQueryWrapper);
        return tag == null ? null : tag.getTagId();
    }

    public Long addTag(int userId, String tagName) {
        Long tagId = existsTag(userId, tagName);
        if (tagId == null) {
            UserTag userTag = new UserTag();
            userTag.setUserId(userId);
            userTag.setTagName(tagName);
            baseMapper.insert(userTag);
            tagId = baseMapper.selectLastInsertId();
        }
        return tagId;
    }

    /**
     * 获取标签列表(菜单用)
     * @param userId
     * @return
     */
    public List<UserTag> getTagList(int userId) {

        UserConfigProperties.UserProperty userProperty = userConfigProperties.getUserProperty(userId);
        if (userProperty.getListTagBy() == TagByEnum.CREATE_TIME_ABS ||
                userProperty.getListTagBy() == TagByEnum.CREATE_TIME_DESC) {
            LambdaQueryWrapper<UserTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.select(UserTag::getTagId, UserTag::getTagName);  // 选择字段
            lambdaQueryWrapper.eq(UserTag::getUserId, userId);
            lambdaQueryWrapper.groupBy(UserTag::getTagId, UserTag::getTagName);  // 分组去重
            if (userProperty.getListTagBy() == TagByEnum.CREATE_TIME_ABS){
                lambdaQueryWrapper.orderByAsc(UserTag::getCreateTime);
            }
            else{
                lambdaQueryWrapper.orderByDesc(UserTag::getCreateTime);
            }
            lambdaQueryWrapper.last("limit " + userProperty.getListTagNum());
            return baseMapper.selectList(lambdaQueryWrapper);
        }
        else{
            return baseMapper.listTagByResourceNum(userId, userProperty.getListTagNum());
        }
    }

    /**
     * 获取标签列表(资源详情用)
     * @param userId
     * @param resourceId
     * @return
     */
    public List<UserTag> getTagList(int userId, Long resourceId) {

        return baseMapper.listTag(userId, resourceId);
    }


}

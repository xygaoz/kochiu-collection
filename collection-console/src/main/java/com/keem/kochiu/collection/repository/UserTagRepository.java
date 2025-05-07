package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.UserTag;
import com.keem.kochiu.collection.enums.TagByEnum;
import com.keem.kochiu.collection.mapper.UserTagMapper;
import com.keem.kochiu.collection.properties.SysConfigProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTagRepository extends ServiceImpl<UserTagMapper, UserTag> {

    private final SysConfigProperties sysConfigProperties;

    public UserTagRepository(SysConfigProperties sysConfigProperties) {
        this.sysConfigProperties = sysConfigProperties;
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
        if (sysConfigProperties.getListTagBy() == TagByEnum.CREATE_TIME_ABS ||
                sysConfigProperties.getListTagBy() == TagByEnum.CREATE_TIME_DESC) {
            LambdaQueryWrapper<UserTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.select(UserTag::getTagId, UserTag::getTagName);  // 选择字段
            lambdaQueryWrapper.eq(UserTag::getUserId, userId);
            lambdaQueryWrapper.groupBy(UserTag::getTagId, UserTag::getTagName);  // 分组去重
            if (sysConfigProperties.getListTagBy() == TagByEnum.CREATE_TIME_ABS){
                lambdaQueryWrapper.orderByAsc(UserTag::getCreateTime);
            }
            else{
                lambdaQueryWrapper.orderByDesc(UserTag::getCreateTime);
            }
            lambdaQueryWrapper.last("limit " + sysConfigProperties.getListTagNum());
            return baseMapper.selectList(lambdaQueryWrapper);
        }
        else{
            return baseMapper.listTagByResourceNum(userId, sysConfigProperties.getListTagNum());
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

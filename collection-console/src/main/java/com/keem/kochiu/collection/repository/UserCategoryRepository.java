package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.UserCategory;
import com.keem.kochiu.collection.enums.CategoryByEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.UserCategoryMapper;
import com.keem.kochiu.collection.properties.SysConfigProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCategoryRepository extends ServiceImpl<UserCategoryMapper, UserCategory>{

    private final SysConfigProperties sysConfigProperties;

    public UserCategoryRepository(SysConfigProperties sysConfigProperties) {
        this.sysConfigProperties = sysConfigProperties;
    }

    /**
     * 获取分类id
     * @param userId
     * @param sno
     * @return
     * @throws CollectionException
     */
    public int getCateId(int userId, int sno) throws CollectionException {

        LambdaQueryWrapper<UserCategory> lambdaQueryWrapper = new LambdaQueryWrapper<UserCategory>()
                .eq(UserCategory::getUserId, userId)
                .eq(UserCategory::getSno, sno);
        try {
            UserCategory userCategory = this.getOne(lambdaQueryWrapper);
            if(userCategory == null){
                return getMinSnoCate(userId);
            }
            return userCategory.getCateId();
        }
        catch (Exception e){
            return getMinSnoCate(userId);
        }
    }

    private int getMinSnoCate(int userId) throws CollectionException {
        LambdaQueryWrapper<UserCategory> lambdaQueryWrapper = new LambdaQueryWrapper<UserCategory>()
                .eq(UserCategory::getUserId, userId)
                .orderByAsc(UserCategory::getSno)
                .last("limit 1");
        UserCategory userCategory = this.getOne(lambdaQueryWrapper);
        if (userCategory == null) {
            throw new CollectionException("获取默认分类失败");
        }
        return userCategory.getCateId();
    }

    /**
     * 获取用户定义的分类列表
     * @param userId
     * @return
     */
    public List<UserCategory> getCategoryList(int userId) {

        if(sysConfigProperties.getListCategoryBy() == CategoryByEnum.CREATE_TIME_ABS ||
                sysConfigProperties.getListCategoryBy() == CategoryByEnum.CREATE_TIME_DESC) {
            LambdaQueryWrapper<UserCategory> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserCategory::getUserId, userId);
            if(sysConfigProperties.getListCategoryBy() == CategoryByEnum.CREATE_TIME_ABS) {
                lambdaQueryWrapper.orderByAsc(UserCategory::getCreateTime);
            }
            else{
                lambdaQueryWrapper.orderByDesc(UserCategory::getCreateTime);
            }
            lambdaQueryWrapper.last("limit " + sysConfigProperties.getListCategoryNum());

            return this.list(lambdaQueryWrapper);
        }
        else{
            return baseMapper.listCategoryByResourceNum(userId, sysConfigProperties.getListCategoryNum());
        }
    }

    public List<UserCategory> getAllCategory(int userId) {

        LambdaQueryWrapper<UserCategory> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserCategory::getUserId, userId);
        return this.list(lambdaQueryWrapper);
    }
}

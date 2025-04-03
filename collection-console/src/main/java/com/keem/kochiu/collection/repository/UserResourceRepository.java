package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.keem.kochiu.collection.data.bo.PageBo;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.SaveTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.UserResourceMapper;
import org.springframework.stereotype.Service;

@Service
public class UserResourceRepository extends ServiceImpl<UserResourceMapper, UserResource>{

    private final UserCategoryRepository categoryRepository;

    public UserResourceRepository(UserCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * 保存资源，返回资源id
     * @param resourceDto
     * @return
     * @throws CollectionException
     */
    public Long saveResource(ResourceDto resourceDto) throws CollectionException {

        UserResource userResource = new UserResource();
        userResource.setUserId(resourceDto.getUserId());
        userResource.setCateId(categoryRepository.getCateId(resourceDto.getUserId(), resourceDto.getCateId()));
        userResource.setSourceFileName(resourceDto.getSourceFileName());
        userResource.setFilePath(resourceDto.getResourceUrl());
        userResource.setSaveType(SaveTypeEnum.LOCAL.getCode());
        userResource.setResourceUrl(resourceDto.getResourceUrl());
        userResource.setFileExt(resourceDto.getFileExt());
        userResource.setResolutionRatio(resourceDto.getResolutionRatio());
        userResource.setSize(resourceDto.getSize());
        userResource.setThumbUrl(resourceDto.getThumbUrl());
        userResource.setThumbRatio(resourceDto.getThumbRatio());
        if (this.save(userResource)) {
            // 获取最后插入的行ID
            Long resourceId = baseMapper.selectLastInsertId();
            userResource.setResourceId(resourceId);
            return resourceId;
        }

        return 0L;
    }

    /**
     * 获取分类下资源列表
     * @param userId
     * @param cateSno
     * @return
     * @throws CollectionException
     */
    public PageInfo<UserResource> getResourceList(int userId, int cateSno, PageBo pageBo) throws CollectionException {

        try(Page<UserResource> page = PageHelper.startPage(pageBo.getPageNum(), pageBo.getPageSize())) {

            LambdaQueryWrapper<UserResource> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserResource::getUserId, userId);
            lambdaQueryWrapper.eq(UserResource::getCateId, categoryRepository.getCateId(userId, cateSno));
            return new PageInfo<>(baseMapper.selectList(lambdaQueryWrapper));
        }
    }
}

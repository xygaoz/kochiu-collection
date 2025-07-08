package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kochiu.collection.data.bo.FilterResourceBo;
import com.kochiu.collection.data.bo.MoveToBo;
import com.kochiu.collection.data.bo.ResInfoBo;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.mapper.UserResourceMapper;
import com.kochiu.collection.properties.UserConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserResourceRepository extends ServiceImpl<UserResourceMapper, UserResource>{

    private final UserConfigProperties userConfigProperties;

    public UserResourceRepository(UserConfigProperties userConfigProperties) {
        this.userConfigProperties = userConfigProperties;
    }

    /**
     * 保存资源，返回资源id
     */
    public Long saveResource(ResourceDto resourceDto, Integer resourceType) {

        UserResource userResource = new UserResource();
        userResource.setUserId(resourceDto.getUserId());
        userResource.setCateId(resourceDto.getCateId());
        userResource.setCataId(resourceDto.getCataId());
        userResource.setSourceFileName(resourceDto.getSourceFileName());
        userResource.setFilePath(resourceDto.getFilePath() == null ? resourceDto.getResourceUrl() : resourceDto.getFilePath());
        userResource.setSaveType(resourceDto.getSaveType().getCode());
        userResource.setResourceUrl(resourceDto.getResourceUrl());
        userResource.setFileExt(resourceDto.getFileExt());
        userResource.setResolutionRatio(resourceDto.getResolutionRatio());
        userResource.setSize(resourceDto.getSize());
        userResource.setThumbUrl(resourceDto.getThumbUrl());
        userResource.setThumbRatio(resourceDto.getThumbRatio());
        userResource.setPreviewUrl(resourceDto.getPreviewUrl());
        userResource.setMd5(resourceDto.getMd5());
        userResource.setResourceType(resourceType);
        if (this.save(userResource)) {
            // 获取最后插入的行ID
            Long resourceId = baseMapper.selectLastInsertId();
            userResource.setResourceId(resourceId);
            return resourceId;
        }

        return 0L;
    }

    private int getPageSize(int userId, Integer pageSize) {

        if (pageSize == null || pageSize <= 0 || pageSize > userConfigProperties.getUserProperty(userId).getResourcePageSize()) {
            return userConfigProperties.getUserProperty(userId).getResourcePageSize();
        }
        return pageSize;
    }

    /**
     * 获取分类下资源列表
     */
    public PageInfo<UserResource> getResourceListByCate(int userId, long cateId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> ignored = PageHelper.startPage(filterResourceBo.getPageNum(), getPageSize(userId, filterResourceBo.getPageSize()))) {

            Set<Integer> typeList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    typeList.add(Objects.requireNonNull(ResourceTypeEnum.getByValue(type)).getCode());
                }
            }
            return new PageInfo<>(baseMapper.selectCategoryResource(userId,
                    cateId,
                    filterResourceBo.getKeyword(),
                    typeList.toArray(new Integer[0]),
                    filterResourceBo.getTags()));
        }
    }

    /**
     * 获取分类下资源列表
     */
    public PageInfo<UserResource> getAllCateResourceList(int userId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> ignored = PageHelper.startPage(filterResourceBo.getPageNum(), getPageSize(userId, filterResourceBo.getPageSize()))) {

            Set<Integer> typeList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    typeList.add(Objects.requireNonNull(ResourceTypeEnum.getByValue(type)).getCode());
                }
            }
            return new PageInfo<>(baseMapper.selectAllCateResource(userId,
                    filterResourceBo.getCateId(),
                    filterResourceBo.getKeyword(),
                    typeList.toArray(new Integer[0]),
                    filterResourceBo.getTags()));
        }
    }

    /**
     * 根据md5查询文件是否存在
     */
    public List<UserResource> countFileMd5(int userId, String md5){

        LambdaQueryWrapper<UserResource> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserResource::getUserId, userId);
        lambdaQueryWrapper.eq(UserResource::getMd5, md5);
        return baseMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 更新资源信息
     */
    public void updateResourceInfo(int userId, ResInfoBo resourceInfo) {

        if(!(StringUtils.isBlank(resourceInfo.getTitle()) && StringUtils.isBlank(resourceInfo.getDescription())
                && resourceInfo.getStar() == null && resourceInfo.getShare() == null)) {
            LambdaUpdateWrapper<UserResource> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(UserResource::getUserId, userId);
            lambdaUpdateWrapper.eq(UserResource::getResourceId, resourceInfo.getResourceId());
            lambdaUpdateWrapper.set(StringUtils.isNotBlank(resourceInfo.getTitle()), UserResource::getTitle, resourceInfo.getTitle());
            lambdaUpdateWrapper.set(StringUtils.isNotBlank(resourceInfo.getDescription()), UserResource::getDescription, resourceInfo.getDescription());
            lambdaUpdateWrapper.set(resourceInfo.getStar() != null, UserResource::getStar, resourceInfo.getStar());
            lambdaUpdateWrapper.set(resourceInfo.getShare() != null, UserResource::getShare, resourceInfo.getShare());

            baseMapper.update(null, lambdaUpdateWrapper);
        }
    }

    /**
     * 获取标签下资源列表
     */
    public PageInfo<UserResource> getResourceListByTag(int userId, int tagId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> ignored = PageHelper.startPage(filterResourceBo.getPageNum(), getPageSize(userId, filterResourceBo.getPageSize()))) {

            Set<Integer> typeList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    typeList.add(Objects.requireNonNull(ResourceTypeEnum.getByValue(type)).getCode());
                }
            }
            return new PageInfo<>(baseMapper.selectTagResource(userId,
                    tagId,
                    filterResourceBo.getKeyword(),
                    typeList.toArray(new Integer[0]))
            );
        }
    }

    /**
     * 获取文件类型下资源列表
     */
    public PageInfo<UserResource> getResourceListByType(int userId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> ignored = PageHelper.startPage(filterResourceBo.getPageNum(), getPageSize(userId, filterResourceBo.getPageSize()))) {

            Set<Integer> typeList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    typeList.add(Objects.requireNonNull(ResourceTypeEnum.getByValue(type)).getCode());
                }
            }
            return new PageInfo<>(baseMapper.selectTypeResource(userId,
                    filterResourceBo.getKeyword(),
                    typeList.toArray(new Integer[0]),
                    filterResourceBo.getTags())
            );
        }
    }

    /**
     * 移动资源到分类
     */
    public void moveToCategory(Integer userId, MoveToBo moveToBo) {

        moveToBo.getResourceIds().forEach(resourceId -> {
            baseMapper.update(null,
                    new LambdaUpdateWrapper<UserResource>()
                            .set(UserResource::getCateId, moveToBo.getCateId())
                            .in(UserResource::getResourceId, moveToBo.getResourceIds())
                            .eq(UserResource::getUserId, userId)
            );
        });
    }

    /**
     * 移动资源到回收站
     */
    public void moveToRecycle(Integer userId, MoveToBo moveToBo){

        moveToBo.getResourceIds().forEach(resourceId -> {
            baseMapper.update(null,
                    new LambdaUpdateWrapper<UserResource>()
                            .set(UserResource::getDeleted, 1)
                            .set(UserResource::getDeleteTime, LocalDateTime.now())
                            .in(UserResource::getResourceId, moveToBo.getResourceIds())
                            .eq(UserResource::getUserId, userId)
            );
        });
    }

    /**
     * 恢复资源
     */
    public void restoreFormRecycle(Integer userId, MoveToBo moveToBo) {

        moveToBo.getResourceIds().forEach(resourceId -> {
            baseMapper.update(null,
                    new LambdaUpdateWrapper<UserResource>()
                            .set(UserResource::getDeleted, 0)
                            .set(UserResource::getDeleteTime, LocalDateTime.now())
                            .in(UserResource::getResourceId, moveToBo.getResourceIds())
                            .eq(UserResource::getUserId, userId)
            );
        });
    }

    /**
     * 删除资源
     */
    public boolean deleteResource(Integer userId, Long resourceId) {

        return baseMapper.delete(new LambdaUpdateWrapper<UserResource>()
                .eq(UserResource::getResourceId, resourceId)
                .eq(UserResource::getUserId, userId)
        ) > 0;
    }

    /**
     * 获取文件类型下资源列表
     */
    public PageInfo<UserResource> getResourceListByRecycle(int userId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> ignored = PageHelper.startPage(filterResourceBo.getPageNum(), getPageSize(userId, filterResourceBo.getPageSize()))) {

            Set<Integer> typeList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    typeList.add(Objects.requireNonNull(ResourceTypeEnum.getByValue(type)).getCode());
                }
            }
            return new PageInfo<>(baseMapper.selectRecycleResource(userId,
                    filterResourceBo.getKeyword(),
                    typeList.toArray(new Integer[0]),
                    filterResourceBo.getTags())
            );
        }
    }

    /**
     * 获取目录下资源列表
     */
    public PageInfo<UserResource> getCatalogResource(int userId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> ignored = PageHelper.startPage(filterResourceBo.getPageNum(), getPageSize(userId, filterResourceBo.getPageSize()))) {

            Set<Integer> typeList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    typeList.add(Objects.requireNonNull(ResourceTypeEnum.getByValue(type)).getCode());
                }
            }

            if(filterResourceBo.isInclude()) {
                return new PageInfo<>(baseMapper.selectCatalogResourceIncludeSub(userId,
                        filterResourceBo.getCataId(),
                        filterResourceBo.getCateId(),
                        filterResourceBo.getKeyword(),
                        typeList.toArray(new Integer[0]),
                        filterResourceBo.getTags())
                );
            }
            else{
                return new PageInfo<>(baseMapper.selectCatalogResource(userId,
                        filterResourceBo.getCataId(),
                        filterResourceBo.getCateId(),
                        filterResourceBo.getKeyword(),
                        typeList.toArray(new Integer[0]),
                        filterResourceBo.getTags())
                );
            }
        }
    }

    /**
     * 获取目录下资源列表
     */
    public List<UserResource> getResources(Integer userId, Long cateId) {

        LambdaQueryWrapper<UserResource> lambdaQueryWrapper = new LambdaQueryWrapper<UserResource>()
                .eq(UserResource::getUserId, userId)
                .eq(UserResource::getCataId, cateId);
        return list(lambdaQueryWrapper);
    }

    // 获取公开资源列表
    public PageInfo<UserResource> getPublicResourceList(int userId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> ignored = PageHelper.startPage(filterResourceBo.getPageNum(), getPageSize(userId, filterResourceBo.getPageSize()))) {

            Set<Integer> typeList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    typeList.add(Objects.requireNonNull(ResourceTypeEnum.getByValue(type)).getCode());
                }
            }
            return new PageInfo<>(baseMapper.selectPublicResource(userId,
                    filterResourceBo.getKeyword(),
                    typeList.toArray(new Integer[0]),
                    filterResourceBo.getTags()));
        }
    }
}

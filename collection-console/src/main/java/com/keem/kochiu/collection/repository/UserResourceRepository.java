package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.keem.kochiu.collection.data.bo.FilterResourceBo;
import com.keem.kochiu.collection.data.bo.MoveToBo;
import com.keem.kochiu.collection.data.bo.ResInfoBo;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.enums.ResourceTypeEnum;
import com.keem.kochiu.collection.enums.SaveTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.UserResourceMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        userResource.setCateId(resourceDto.getCateId());
        userResource.setSourceFileName(resourceDto.getSourceFileName());
        userResource.setFilePath(resourceDto.getResourceUrl());
        userResource.setSaveType(SaveTypeEnum.LOCAL.getCode());
        userResource.setResourceUrl(resourceDto.getResourceUrl());
        userResource.setFileExt(resourceDto.getFileExt());
        userResource.setResolutionRatio(resourceDto.getResolutionRatio());
        userResource.setSize(resourceDto.getSize());
        userResource.setThumbUrl(resourceDto.getThumbUrl());
        userResource.setThumbRatio(resourceDto.getThumbRatio());
        userResource.setPreviewUrl(resourceDto.getPreviewUrl());
        userResource.setMd5(resourceDto.getMd5());
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
    public PageInfo<UserResource> getResourceListByCate(int userId, int cateSno, FilterResourceBo filterResourceBo) throws CollectionException {

        try(Page<UserResource> page = PageHelper.startPage(filterResourceBo.getPageNum(), filterResourceBo.getPageSize())) {

            Set<String> fileExtList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    fileExtList.addAll(FileTypeEnum.getNames(ResourceTypeEnum.getByValue(type)));
                }
            }
            return new PageInfo<>(baseMapper.selectCategoryResource(userId,
                    categoryRepository.getCateId(userId, cateSno),
                    filterResourceBo.getKeyword(),
                    fileExtList.toArray(new String[0]),
                    filterResourceBo.getTags()));
        }
    }

    /**
     * 获取分类下资源列表
     * @param userId
     * @return
     * @throws CollectionException
     */
    public PageInfo<UserResource> getAllResourceList(int userId, FilterResourceBo filterResourceBo) throws CollectionException {

        try(Page<UserResource> page = PageHelper.startPage(filterResourceBo.getPageNum(), filterResourceBo.getPageSize())) {

            Set<String> fileExtList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    fileExtList.addAll(FileTypeEnum.getNames(ResourceTypeEnum.getByValue(type)));
                }
            }
            return new PageInfo<>(baseMapper.selectAllResource(userId,
                    filterResourceBo.getCateId(),
                    filterResourceBo.getKeyword(),
                    fileExtList.toArray(new String[0]),
                    filterResourceBo.getTags()));
        }
    }

    /**
     * 根据md5查询文件是否存在
     * @param userId
     * @param md5
     * @return
     */
    public List<UserResource> countFileMd5(int userId, String md5){

        LambdaQueryWrapper<UserResource> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserResource::getUserId, userId);
        lambdaQueryWrapper.eq(UserResource::getMd5, md5);
        return baseMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 更新资源信息
     * @param userId
     * @param resourceInfo
     */
    public void updateResourceInfo(int userId, ResInfoBo resourceInfo) {

        if(!(StringUtils.isBlank(resourceInfo.getTitle()) && StringUtils.isBlank(resourceInfo.getDescription())
                && resourceInfo.getStar() == null)) {
            LambdaUpdateWrapper<UserResource> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(UserResource::getUserId, userId);
            lambdaUpdateWrapper.eq(UserResource::getResourceId, resourceInfo.getResourceId());
            lambdaUpdateWrapper.set(StringUtils.isNotBlank(resourceInfo.getTitle()), UserResource::getTitle, resourceInfo.getTitle());
            lambdaUpdateWrapper.set(StringUtils.isNotBlank(resourceInfo.getDescription()), UserResource::getDescription, resourceInfo.getDescription());
            lambdaUpdateWrapper.set(resourceInfo.getStar() != null, UserResource::getStar, resourceInfo.getStar());

            baseMapper.update(null, lambdaUpdateWrapper);
        }
    }

    /**
     * 获取标签下资源列表
     * @param userId
     * @param tagId
     * @return
     */
    public PageInfo<UserResource> getResourceListByTag(int userId, int tagId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> page = PageHelper.startPage(filterResourceBo.getPageNum(), filterResourceBo.getPageSize())) {

            Set<String> fileExtList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    fileExtList.addAll(FileTypeEnum.getNames(ResourceTypeEnum.getByValue(type)));
                }
            }
            return new PageInfo<>(baseMapper.selectTagResource(userId,
                    tagId,
                    filterResourceBo.getKeyword(),
                    fileExtList.toArray(new String[0]))
            );
        }
    }

    /**
     * 获取文件类型下资源列表
     * @param userId
     * @return
     */
    public PageInfo<UserResource> getResourceListByType(int userId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> page = PageHelper.startPage(filterResourceBo.getPageNum(), filterResourceBo.getPageSize())) {

            Set<String> fileExtList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    fileExtList.addAll(FileTypeEnum.getNames(ResourceTypeEnum.getByValue(type)));
                }
            }
            return new PageInfo<>(baseMapper.selectTypeResource(userId,
                    filterResourceBo.getKeyword(),
                    fileExtList.toArray(new String[0]),
                    filterResourceBo.getTags())
            );
        }
    }

    /**
     * 移动资源到分类
     * @param userId
     * @param moveToBo
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
     * @param userId
     * @param moveToBo
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
     * @param userId
     * @param moveToBo
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
     * @param userId
     * @param resourceId
     * @return
     */
    public boolean deleteResource(Integer userId, Long resourceId) {

        return baseMapper.delete(new LambdaUpdateWrapper<UserResource>()
                .eq(UserResource::getResourceId, resourceId)
                .eq(UserResource::getUserId, userId)
        ) > 0;
    }

    /**
     * 获取文件类型下资源列表
     * @param userId
     * @return
     */
    public PageInfo<UserResource> getResourceListByRecycle(int userId, FilterResourceBo filterResourceBo) {

        try(Page<UserResource> page = PageHelper.startPage(filterResourceBo.getPageNum(), filterResourceBo.getPageSize())) {

            Set<String> fileExtList = new HashSet<>();
            if(filterResourceBo.getTypes() != null) {
                for (String type : filterResourceBo.getTypes()) {
                    fileExtList.addAll(FileTypeEnum.getNames(ResourceTypeEnum.getByValue(type)));
                }
            }
            return new PageInfo<>(baseMapper.selectRecycleResource(userId,
                    filterResourceBo.getKeyword(),
                    fileExtList.toArray(new String[0]),
                    filterResourceBo.getTags())
            );
        }
    }
}

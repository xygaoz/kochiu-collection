package com.kochiu.collection.service;

import com.github.pagehelper.PageInfo;
import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.bo.*;
import com.kochiu.collection.data.dto.TagDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.PageVo;
import com.kochiu.collection.data.vo.ResourceVo;
import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserCatalog;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.entity.UserTag;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.SaveTypeEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.properties.CollectionProperties;
import com.kochiu.collection.repository.*;
import com.kochiu.collection.service.file.FileStrategyFactory;
import com.kochiu.collection.service.store.ResourceStoreStrategy;
import com.kochiu.collection.service.store.ResourceStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.kochiu.collection.enums.ErrorCodeEnum.CONTENT_CANNOT_BE_EMPTY;

@Slf4j
@Service
public class UserResourceService {

    @Value("${server.servlet.context-path}")
    private String contextPath = "";

    private final ResourceStrategyFactory resourceStrategyFactory;
    private final SysUserRepository userRepository;
    private final UserResourceRepository resourceRepository;
    private final UserTagRepository tagRepository;
    private final UserCategoryRepository categoryRepository;
    private final UserCatalogRepository catalogRepository;
    private final FileStrategyFactory fileStrategyFactory;

    public UserResourceService(ResourceStrategyFactory resourceStrategyFactory,
                               SysUserRepository userRepository,
                               UserResourceRepository resourceRepository,
                               UserTagRepository tagRepository,
                               UserCategoryRepository categoryRepository,
                               UserCatalogRepository catalogRepository,
                               FileStrategyFactory fileStrategyFactory) {
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.catalogRepository = catalogRepository;
        this.fileStrategyFactory = fileStrategyFactory;
    }


    /**
     * 获取用户分类资源列表
     */
    public PageVo<ResourceVo> getResourceListByCate(UserDto userDto,
                                                    Long cateId,
                                                    FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceListByCate(user.getUserId(), cateId, filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 获取用户所有分类资源列表
     */
    public PageVo<ResourceVo> getAllResourceList(UserDto userDto, FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getAllCateResourceList(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 构建url
     */
    private String buildResourceUrl(SysUser user, UserResource resource){

        if(SaveTypeEnum.getByCode(resource.getSaveType()) != SaveTypeEnum.NETWORK){
            return contextPath + "/resource/" + resource.getResourceId() + "/" + resource.getResourceUrl().replace("/" + user.getUserCode() + "/", "");
        }
        return resource.getThumbUrl();
    }

    /**
     * 更新资源信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateResourceInfo(UserDto userDto, ResInfoBo resourceInfo) throws CollectionException {

        if(StringUtils.isBlank(resourceInfo.getTitle()) && StringUtils.isBlank(resourceInfo.getDescription())
            && resourceInfo.getStar() == null){
            throw new CollectionException(CONTENT_CANNOT_BE_EMPTY);
        }

        SysUser user = userRepository.getUser(userDto);
        resourceRepository.updateResourceInfo(user.getUserId(), resourceInfo);
    }

    /**
     * 获取用户标签资源列表
     */
    public PageVo<ResourceVo> getResourceListByTag(UserDto userDto,
                                                   int tagId,
                                                   FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceListByTag(user.getUserId(), tagId, filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 获取文件类型签资源列表
     */
    public PageVo<ResourceVo> getResourceListByType(UserDto userDto,
                                                   FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceListByType(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 构建资源列表
     */
    private PageVo<ResourceVo> buildResourceList(SysUser user, PageInfo<UserResource> resourceList) {
        List<ResourceVo> datas = resourceList
                .getList()
                .stream()
                .map(resource -> {
                            //缩略图宽高
                            String thumbRatio = resource.getThumbRatio();
                            int width = 150;
                            int height = 200;
                            if(thumbRatio != null){
                                String[] split = thumbRatio.split("x");
                                width = Integer.parseInt(split[0]);
                                height = Integer.parseInt(split[1]);
                            }

                            FileType fileType = fileStrategyFactory.getFileType(resource.getFileExt());

                            //获取资源标签
                            List<UserTag> tags = tagRepository.getTagList(user.getUserId(), resource.getResourceId());

                            return ResourceVo.builder()
                                    .resourceId(resource.getResourceId())
                                    .resourceUrl(buildResourceUrl(user, resource))
                                    .thumbnailUrl(StringUtils.isNotBlank(resource.getThumbUrl()) ? contextPath + "/resource/" + resource.getResourceId() + "/" + resource.getThumbUrl().replace("/" + user.getUserCode() + "/", "") : null)
                                    .previewUrl(StringUtils.isNotBlank(resource.getPreviewUrl()) ? contextPath + "/resource/" + resource.getResourceId() + "/" + resource.getPreviewUrl().replace("/" + user.getUserCode() + "/", "") : null)
                                    .title(resource.getTitle())
                                    .description(resource.getDescription())
                                    .sourceFileName(resource.getSourceFileName())
                                    .cateName(resource.getCateName())
                                    .cataPath(resource.getCataPath())
                                    .width(width)
                                    .height(height)
                                    .fileType(resource.getFileExt() + " - " +fileType.desc().getLabel())
                                    .typeName(fileType.desc().name().toLowerCase())
                                    .mimeType(fileType.mimeType())
                                    .size(resource.getSize())
                                    .resolutionRatio(resource.getResolutionRatio())
                                    .createTime(resource.getCreateTime())
                                    .updateTime(resource.getUpdateTime())
                                    .share(resource.getShare() == 1)
                                    .star(resource.getStar())
                                    .tags(tags.stream().map(tag -> TagDto.builder()
                                            .tagId(tag.getTagId())
                                            .tagName(tag.getTagName())
                                            .build()).toList())
                                    .build();
                        }
                ).toList();

        return PageVo.<ResourceVo>builder()
                .list(datas)
                .pageNum(resourceList.getPageNum())
                .pageSize(resourceList.getPageSize())
                .total(resourceList.getTotal())
                .pages(resourceList.getPages())
                .build();
    }

    /**
     * 批量更新
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(UserDto userDto, BatchUpdateBo batchUpdateBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        batchUpdateBo.getResourceIds().forEach(resourceId -> {
            ResInfoBo resourceInfo = ResInfoBo.builder()
                    .resourceId(resourceId)
                    .title(batchUpdateBo.getTitle())
                    .description(batchUpdateBo.getDescription())
                    .share(batchUpdateBo.getShare())
                    .build();
            resourceRepository.updateResourceInfo(user.getUserId(), resourceInfo);
        });
    }

    /**
     * 批量移动到分类
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveToCategory(UserDto userDto, MoveToBo moveToBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        if(categoryRepository.getById(moveToBo.getCateId()) == null){
            throw new CollectionException(ErrorCodeEnum.CATEGORY_NOT_EXIST);
        }
        resourceRepository.moveToCategory(user.getUserId(), moveToBo);
    }

    /**
     * 批量移动到回收站
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveToRecycle(UserDto userDto, MoveToBo moveToBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        if(!moveToBo.isDeleted()) {
            resourceRepository.moveToRecycle(user.getUserId(), moveToBo);
        }
        else{
            moveToBo.getResourceIds().forEach(resourceId -> {
                //先删除资源文件
                resourceStrategyFactory.getStrategy(user.getStrategy()).deleteFile(user.getUserId(), resourceId);
                resourceRepository.deleteResource(user.getUserId(), resourceId);
            });
        }
    }

    /**
     * 批量从回收站恢复
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreFormRecycle(UserDto userDto, MoveToBo moveToBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        resourceRepository.restoreFormRecycle(user.getUserId(), moveToBo);
    }

    /**
     * 获取文件类型签资源列表
     */
    public PageVo<ResourceVo> getResourceListByRecycle(UserDto userDto,
                                                    FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceListByRecycle(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 获取目录下资源列表
     */
    public PageVo<ResourceVo> getResourceListByCatalog(UserDto userDto,
                                                       FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getCatalogResource(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 批量移动到目录
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveToCatalog(UserDto userDto, MoveToCataBo moveToBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        UserCatalog catalog = catalogRepository.getById(moveToBo.getCataId());
        if(catalog == null){
            throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
        }

        //先克隆资源对象，因为后面先变数据库再变文件
        List<UserResource> resources = new ArrayList<>();
        for(Long resourceId : moveToBo.getResourceIds()) {
            //假如目录相同，跳过
            UserResource resource = resourceRepository.getById(resourceId);
            if(resource.getCataId().equals(moveToBo.getCataId())){
                continue;
            }
            resources.add(resource.clone());
        }
        if(resources.isEmpty()){
            return;
        }

        ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
        //更新数据库
        resourceStoreStrategy.updateResourcesPath(user.getUserId(), moveToBo.getCataId(), moveToBo.getResourceIds());
        //移动物理文件
        for(UserResource resource : resources){
            resourceStoreStrategy.moveFile(user.getUserId(), resource,
                    ("/" + user.getUserCode() + "/" + catalog.getCataPath()).replaceAll("//", "/"));
        }
    }

    //获取公开资源列表
    public PageVo<ResourceVo> getPublicResourceList(UserDto userDto, FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getPublicResourceList(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    //设置是否公开
    @Transactional(rollbackFor = Exception.class)
    public void setResourcePublic(UserDto userDto, Long resourceId, boolean isPublic) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            throw new CollectionException(ErrorCodeEnum.RESOURCE_NOT_EXIST);
        }
        else if(!resource.getUserId().equals(user.getUserId())){
            throw new CollectionException(ErrorCodeEnum.RESOURCE_NOT_EXIST);
        }

        resource.setShare(isPublic ? 1 : 0);
        resourceRepository.updateById(resource);
    }
}

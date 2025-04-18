package com.keem.kochiu.collection.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.pagehelper.PageInfo;
import com.keem.kochiu.collection.data.bo.*;
import com.keem.kochiu.collection.data.dto.TagDto;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.data.vo.PageVo;
import com.keem.kochiu.collection.data.vo.ResourceVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCatalog;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.entity.UserTag;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.enums.SaveTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.repository.*;
import com.keem.kochiu.collection.service.store.ResourceStoreStrategy;
import com.keem.kochiu.collection.service.store.ResourceStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.*;

@Slf4j
@Service
public class UserResourceService {

    private final ResourceStrategyFactory resourceStrategyFactory;
    private final SysUserRepository userRepository;
    private final UserResourceRepository resourceRepository;
    private final CollectionProperties properties;
    private final UserTagRepository tagRepository;
    private final UserCategoryRepository categoryRepository;
    private final UserCatalogRepository catalogRepository;
    private final UserCatalogService userCatalogService;

    public UserResourceService(ResourceStrategyFactory resourceStrategyFactory,
                               SysUserRepository userRepository,
                               UserResourceRepository resourceRepository,
                               CollectionProperties properties,
                               UserTagRepository tagRepository,
                               UserCategoryRepository categoryRepository,
                               UserCatalogRepository catalogRepository,
                               UserCatalogService userCatalogService) {
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.properties = properties;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.catalogRepository = catalogRepository;
        this.userCatalogService = userCatalogService;
    }

    /**
     * 通过策略保存文件
     * @param uploadBo
     * @return
     * @throws CollectionException
     */
    @Transactional(rollbackFor = Exception.class)
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        try {
            String md5 = DigestUtil.md5Hex(DigestUtil.md5Hex(uploadBo.getFile().getBytes()));
            List<UserResource> resources =resourceRepository.countFileMd5(user.getUserId(), md5);
            if(!resources.isEmpty() && !uploadBo.isOverwrite()){
                throw new CollectionException(FILE_IS_EXIST);
            }

            UserCatalog catalog = catalogRepository.getById(uploadBo.getCataId());
            if(catalog == null){
                throw new CollectionException(ErrorCodeEnum.CATEGORY_NOT_EXIST);
            }
            if(!uploadBo.isAutoCreate() && (uploadBo.getCataId() == null || catalogRepository.getById(uploadBo.getCataId()) == null)){
                //非自动创建目录，检查目录是否存在
                throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
            }

            String path;
            if(uploadBo.isAutoCreate()) {
                String catalogPath = ResourceStoreStrategy.dateFormat.format(System.currentTimeMillis());
                path = "/" + user.getUserCode() + "/" + catalogPath;
                //创建目录
                Long id = userCatalogService.addCatalogPath(userDto, catalogPath);
                if(id == null){
                    throw new CollectionException(ErrorCodeEnum.CATALOG_CREATE_FAILURE);
                }
                else{
                    uploadBo.setCataId(id);
                }
            }
            else{
                path = "/" + user.getUserCode() + "/" + catalog.getCataPath();
            }
            path = path.replaceAll("//", "/");
            if(path.endsWith("/")){
                path = path.substring(0, path.length() - 1);
            }

            FileVo fileVo = resourceStrategyFactory.getStrategy(user.getStrategy())
                    .saveFile(uploadBo, userDto, md5, path);
            //保存成功。删除原有记录
            if(!resources.isEmpty() && uploadBo.isOverwrite()){
                resourceRepository.removeById(resources.get(0).getResourceId(), true);
            }
            return fileVo;
        }catch (IOException e){
            log.error("文件保存失败", e);
            throw new CollectionException(FILE_SAVING_FAILURE);
        }
    }

    /**
     * 通过策略下载文件
     * @param request
     * @param response
     * @param resourceId
     */
    public void download(HttpServletRequest request, HttpServletResponse response, Long resourceId) {

        //查找资源
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            response.setStatus(404);
            return;
        }

        int userId = resource.getUserId();
        SysUser user = userRepository.getById(userId);
        if (user == null) {
            response.setStatus(404);
            return;
        }

        resourceStrategyFactory.getStrategy(user.getStrategy()).download(request, response, resourceId);
    }

    /**
     * 获取用户分类资源列表
     * @param userDto
     * @param cateSno
     * @return
     * @throws CollectionException
     */
    public PageVo<ResourceVo> getResourceListByCate(UserDto userDto,
                                                    int cateSno,
                                                    FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceListByCate(user.getUserId(), cateSno, filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 获取用户所有分类资源列表
     * @param userDto
     * @return
     * @throws CollectionException
     */
    public PageVo<ResourceVo> getAllResourceList(UserDto userDto, FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getAllResourceList(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 构建url
     * @param user
     * @param resource
     * @return
     */
    private String buildResourceUrl(SysUser user, UserResource resource){

        if(SaveTypeEnum.getByCode(resource.getSaveType()) != SaveTypeEnum.NETWORK){
            return "/resource/" + resource.getResourceId() + "/" + resource.getResourceUrl().replace("/" + user.getUserCode() + "/", "");
        }
        return resource.getThumbUrl();
    }

    /**
     * 更新资源信息
     * @param userDto
     * @param resourceInfo
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
     * @param userDto
     * @param tagId
     * @return
     * @throws CollectionException
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
     * @param userDto
     * @return
     * @throws CollectionException
     */
    public PageVo<ResourceVo> getResourceListByType(UserDto userDto,
                                                   FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceListByType(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 构建资源列表
     * @param user
     * @param resourceList
     * @return
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

                            FileTypeEnum fileType = FileTypeEnum.getByValue(resource.getFileExt());

                            //获取资源标签
                            List<UserTag> tags = tagRepository.getTagList(user.getUserId(), resource.getResourceId());

                            return ResourceVo.builder()
                                    .resourceId(resource.getResourceId())
                                    .resourceUrl(buildResourceUrl(user, resource))
                                    .thumbnailUrl(StringUtils.isNotBlank(resource.getThumbUrl()) ? "/resource/" + resource.getResourceId() + "/" + resource.getThumbUrl().replace("/" + user.getUserCode() + "/", "") : null)
                                    .previewUrl(StringUtils.isNotBlank(resource.getPreviewUrl()) ? "/resource/" + resource.getResourceId() + "/" + resource.getPreviewUrl().replace("/" + user.getUserCode() + "/", "") : null)
                                    .title(resource.getTitle())
                                    .description(resource.getDescription())
                                    .sourceFileName(resource.getSourceFileName())
                                    .cateName(resource.getCateName())
                                    .width(width)
                                    .height(height)
                                    .fileType(resource.getFileExt() + " / " +fileType.getDesc().getLabel())
                                    .typeName(properties.getResourceType(resource.getFileExt()).name().toLowerCase())
                                    .mimeType(fileType.getMimeType())
                                    .size(resource.getSize())
                                    .resolutionRatio(resource.getResolutionRatio())
                                    .createTime(resource.getCreateTime())
                                    .updateTime(resource.getUpdateTime())
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
     * @param userDto
     * @param batchUpdateBo
     * @throws CollectionException
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(UserDto userDto, BatchUpdateBo batchUpdateBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        batchUpdateBo.getResourceIds().forEach(resourceId -> {
            ResInfoBo resourceInfo = ResInfoBo.builder()
                    .resourceId(resourceId)
                    .title(batchUpdateBo.getTitle())
                    .description(batchUpdateBo.getDescription())
                    .build();
            resourceRepository.updateResourceInfo(user.getUserId(), resourceInfo);
        });
    }

    /**
     * 批量移动到分类
     * @param userDto
     * @param moveToBo
     * @throws CollectionException
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
     * @param userDto
     * @param moveToBo
     * @throws CollectionException
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
     * @param userDto
     * @param moveToBo
     * @throws CollectionException
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreFormRecycle(UserDto userDto, MoveToBo moveToBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        resourceRepository.restoreFormRecycle(user.getUserId(), moveToBo);
    }

    /**
     * 获取文件类型签资源列表
     * @param userDto
     * @return
     * @throws CollectionException
     */
    public PageVo<ResourceVo> getResourceListByRecycle(UserDto userDto,
                                                    FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceListByRecycle(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 获取目录下资源列表
     * @param userDto
     * @return
     * @throws CollectionException
     */
    public PageVo<ResourceVo> getResourceListByCatalog(UserDto userDto,
                                                       FilterResourceBo filterResourceBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getCatalogResource(user.getUserId(), filterResourceBo);
        return buildResourceList(user, resourceList);
    }

    /**
     * 批量移动到目录
     * @param userDto
     * @param moveToBo
     * @throws CollectionException
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
            resources.add(resourceRepository.getById(resourceId).clone());
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
}

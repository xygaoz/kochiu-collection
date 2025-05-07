package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.Add;
import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.annotation.Edit;
import com.keem.kochiu.collection.annotation.Remove;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.*;
import com.keem.kochiu.collection.data.dto.TagDto;
import com.keem.kochiu.collection.data.vo.PageVo;
import com.keem.kochiu.collection.data.vo.ResourceVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.UserResourceService;
import com.keem.kochiu.collection.service.UserResourceTagService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
public class UserResourceController {

    private final UserResourceService resourceService;
    private final UserResourceTagService tagService;
    private final static String RESOURCE_PATH = PUBLIC_URL + "/resource";

    public UserResourceController(UserResourceService resourceService,
                                  UserResourceTagService tagService) {
        this.resourceService = resourceService;
        this.tagService = tagService;
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/category/{cateId}")
    public DefaultResult<PageVo<ResourceVo>> getResourceListByCate(@PathVariable Long cateId,
                                                                   FilterResourceBo filterResourceBo) throws CollectionException {
        return DefaultResult.ok(resourceService.getResourceListByCate(CheckPermitAspect.USER_INFO.get(), cateId, filterResourceBo));
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/updateInfo")
    public DefaultResult<Boolean> updateResourceInfo(@Valid ResInfoBo resourceInfo) throws CollectionException {
        resourceService.updateResourceInfo(CheckPermitAspect.USER_INFO.get(), resourceInfo);
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/addTag")
    public DefaultResult<TagDto> addResourceTag(@Validated({Add.class}) TagDto resourceInfo) throws CollectionException {
        return DefaultResult.ok(tagService.addResourceTag(CheckPermitAspect.USER_INFO.get(), resourceInfo));
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/removeTag")
    public DefaultResult<Boolean> removeResourceTag(@Validated({Remove.class}) TagDto tagDto) throws CollectionException {
        tagService.removeResourceTag(CheckPermitAspect.USER_INFO.get(), tagDto);
        return DefaultResult.ok(true);
    }

    /**
     * 根据标签获取资源列表
     * @param tagId
     * @param filterResourceBo
     * @return
     * @throws CollectionException
     */
    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/tag/{tagId}")
    public DefaultResult<PageVo<ResourceVo>> getResourceListByTag(@PathVariable int tagId,
                                                                  FilterResourceBo filterResourceBo) throws CollectionException {
        return DefaultResult.ok(resourceService.getResourceListByTag(CheckPermitAspect.USER_INFO.get(), tagId, filterResourceBo));
    }

    /**
     * 根据文件类型获取资源列表
     * @param filterResourceBo
     * @return
     * @throws CollectionException
     */
    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/type/{type}")
    public DefaultResult<PageVo<ResourceVo>> getResourceListByType(@PathVariable String type,
                                                                  FilterResourceBo filterResourceBo) throws CollectionException {
        filterResourceBo.setTypes(new String[]{type});
        return DefaultResult.ok(resourceService.getResourceListByType(CheckPermitAspect.USER_INFO.get(), filterResourceBo));
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/batchUpdate")
    public DefaultResult<Boolean> batchUpdate(@Validated BatchUpdateBo batchUpdateBo) throws CollectionException {
        resourceService.batchUpdate(CheckPermitAspect.USER_INFO.get(), batchUpdateBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/batchAddTag")
    public DefaultResult<TagDto> batchAddTag(@Validated({Add.class}) BatchTagBo tagDto) throws CollectionException {
        return DefaultResult.ok(tagService.batchAddTag(CheckPermitAspect.USER_INFO.get(), tagDto));
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/batchRemoveTag")
    public DefaultResult<Boolean> batchRemoveTag(@Validated({Remove.class}) BatchTagBo tagDto) throws CollectionException {
        tagService.batchRemoveTag(CheckPermitAspect.USER_INFO.get(), tagDto);
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/moveToCategory")
    public DefaultResult<Boolean> moveToCategory(@Validated({Edit.class}) MoveToBo moveToBo) throws CollectionException {
        resourceService.moveToCategory(CheckPermitAspect.USER_INFO.get(), moveToBo);
        return DefaultResult.ok(true);
    }

    /**
     * 移动到回收站/或直接删除
     * @param moveToBo
     * @return
     * @throws CollectionException
     */
    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/moveToRecycle")
    public DefaultResult<Boolean> moveToRecycle(@Validated MoveToBo moveToBo) throws CollectionException {
        resourceService.moveToRecycle(CheckPermitAspect.USER_INFO.get(), moveToBo);
        return DefaultResult.ok(true);
    }

    /**
     * 从回收站恢复
     * @param moveToBo
     * @return
     * @throws CollectionException
     */
    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/restore")
    public DefaultResult<Boolean> restoreFormRecycle(@Validated MoveToBo moveToBo) throws CollectionException {
        resourceService.restoreFormRecycle(CheckPermitAspect.USER_INFO.get(), moveToBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/moveToCatalog")
    public DefaultResult<Boolean> moveToCatalog(@Validated({Edit.class}) MoveToCataBo moveToBo) throws CollectionException {
        resourceService.moveToCatalog(CheckPermitAspect.USER_INFO.get(), moveToBo);
        return DefaultResult.ok(true);
    }

    /**
     * 根据文件类型获取资源列表
     * @param filterResourceBo
     * @return
     * @throws CollectionException
     */
    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/recycle")
    public DefaultResult<PageVo<ResourceVo>> getResourceListByRecycle(FilterResourceBo filterResourceBo) throws CollectionException {
        return DefaultResult.ok(resourceService.getResourceListByRecycle(CheckPermitAspect.USER_INFO.get(), filterResourceBo));
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/all-category")
    public DefaultResult<PageVo<ResourceVo>> getAllResourceList(FilterResourceBo filterResourceBo) throws CollectionException {
        return DefaultResult.ok(resourceService.getAllResourceList(CheckPermitAspect.USER_INFO.get(), filterResourceBo));
    }

    /**
     * 获取目录下资源列表
     * @param filterResourceBo
     * @return
     * @throws CollectionException
     */
    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/catalog/{cataId}")
    public DefaultResult<PageVo<ResourceVo>> getResourceListByCatalog(@PathVariable long cataId,
                                                                  FilterResourceBo filterResourceBo) throws CollectionException {
        filterResourceBo.setCataId(cataId);
        return DefaultResult.ok(resourceService.getResourceListByCatalog(CheckPermitAspect.USER_INFO.get(), filterResourceBo));
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/public")
    public DefaultResult<PageVo<ResourceVo>> getPublicResourceList(FilterResourceBo filterResourceBo) throws CollectionException {
        return DefaultResult.ok(resourceService.getPublicResourceList(CheckPermitAspect.USER_INFO.get(), filterResourceBo));
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/set-public/{resourceId}")
    public void setResourcePublic(@NotNull @PathVariable Long resourceId, boolean isPublic) throws CollectionException {
        resourceService.setResourcePublic(CheckPermitAspect.USER_INFO.get(), resourceId, isPublic);
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/batch-share")
    public DefaultResult<Boolean> batchShareResources(@Validated BatchUpdateBo batchUpdateBo) throws CollectionException {
        resourceService.batchUpdate(CheckPermitAspect.USER_INFO.get(), batchUpdateBo);
        return DefaultResult.ok(true);
    }
}
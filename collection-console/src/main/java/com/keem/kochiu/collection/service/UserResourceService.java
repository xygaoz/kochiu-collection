package com.keem.kochiu.collection.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.pagehelper.PageInfo;
import com.keem.kochiu.collection.data.bo.PageBo;
import com.keem.kochiu.collection.data.bo.ResInfoBo;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.data.vo.PageVo;
import com.keem.kochiu.collection.data.vo.ResourceVo;
import com.keem.kochiu.collection.data.dto.TagDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.entity.UserResourceTag;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.enums.SaveTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserResourceRepository;
import com.keem.kochiu.collection.repository.UserResourceTagRepository;
import com.keem.kochiu.collection.service.store.ResourceStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class UserResourceService {

    private final ResourceStrategyFactory resourceStrategyFactory;
    private final SysUserRepository userRepository;
    private final UserResourceRepository resourceRepository;
    private final CollectionProperties properties;
    private final UserResourceTagRepository tagRepository;

    public UserResourceService(ResourceStrategyFactory resourceStrategyFactory,
                               SysUserRepository userRepository,
                               UserResourceRepository resourceRepository,
                               CollectionProperties properties,
                               UserResourceTagRepository tagRepository) {
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.properties = properties;
        this.tagRepository = tagRepository;
    }

    /**
     * 通过策略保存文件
     * @param uploadBo
     * @return
     * @throws CollectionException
     */
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        try {
            String md5 = DigestUtil.md5Hex(DigestUtil.md5Hex(uploadBo.getFile().getBytes()));
            List<UserResource> resources =resourceRepository.countFileMd5(user.getUserId(), md5);
            if(!resources.isEmpty() && !uploadBo.isOverwrite()){
                throw new CollectionException("文件已存在");
            }

            FileVo fileVo = resourceStrategyFactory.getStrategy(user.getStrategy())
                    .saveFile(uploadBo, userDto, md5);
            //保存成功。删除原有记录
            if(!resources.isEmpty() && uploadBo.isOverwrite()){
                resourceRepository.removeById(resources.get(0).getResourceId(), true);
            }
            return fileVo;
        }catch (IOException e){
            log.error("文件保存失败", e);
            throw new CollectionException("文件保存失败");
        }
    }

    /**
     * 通过策略下载文件
     * @param request
     * @param response
     * @param resourceId
     */
    public void download(HttpServletRequest request, HttpServletResponse response, int resourceId) {

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
    public PageVo<ResourceVo> getResourceList(UserDto userDto,
                                                int cateSno,
                                                PageBo pageBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        PageInfo<UserResource> resourceList = resourceRepository.getResourceList(user.getUserId(), cateSno, pageBo);
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
                        List<UserResourceTag> tags = tagRepository.getTagList(user.getUserId(), resource.getResourceId());

                        return ResourceVo.builder()
                                .resourceId(resource.getResourceId())
                                .resourceUrl(buildResourceUrl(user, resource))
                                .thumbnailUrl(StringUtils.isNotBlank(resource.getThumbUrl()) ? "/resource/" + resource.getResourceId() + "/" + resource.getThumbUrl().replace("/" + user.getUserCode() + "/", "") : null)
                                .previewUrl(StringUtils.isNotBlank(resource.getPreviewUrl()) ? "/resource/" + resource.getResourceId() + "/" + resource.getPreviewUrl().replace("/" + user.getUserCode() + "/", "") : null)
                                .title(resource.getTitle())
                                .description(resource.getDescription())
                                .sourceFileName(resource.getSourceFileName())
                                .width(width)
                                .height(height)
                                .fileType(fileType.getDesc())
                                .typeName(properties.getResourceType(resource.getFileExt()).name().toLowerCase())
                                .mimeType(fileType.getMimeType())
                                .size(resource.getSize())
                                .resolutionRatio(resource.getResolutionRatio())
                                .createTime(resource.getCreateTime())
                                .updateTime(resource.getUpdateTime())
                                .star(resource.getStar())
                                .tags(tags.stream().map(tag -> new TagDto(tag.getTagId(), tag.getTagName())).toList())
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
    public void updateResourceInfo(UserDto userDto, ResInfoBo resourceInfo) throws CollectionException {

        if(StringUtils.isBlank(resourceInfo.getTitle()) && StringUtils.isBlank(resourceInfo.getDescription())
            && resourceInfo.getStar() == null){
            throw new CollectionException("更新内容不能为空");
        }

        SysUser user = userRepository.getUser(userDto);
        resourceRepository.updateResourceInfo(user.getUserId(), resourceInfo);
    }

    /**
     * 添加资源标签
     * @param userDto
     * @param resourceInfo
     * @return
     * @throws CollectionException
     */
    public TagDto addResourceTag(UserDto userDto, ResInfoBo resourceInfo) throws CollectionException {

        if(StringUtils.isBlank(resourceInfo.getTagName())){
            throw new CollectionException("标签不能为空");
        }
        SysUser user = userRepository.getUser(userDto);
        return resourceRepository.addTag(user.getUserId(), resourceInfo);
    }

    /**
     * 删除资源标签
     * @param userDto
     * @param tagDto
     * @throws CollectionException
     */
    public void removeResourceTag(UserDto userDto, TagDto tagDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        resourceRepository.removeResourceTag(user.getUserId(), tagDto);
    }
}

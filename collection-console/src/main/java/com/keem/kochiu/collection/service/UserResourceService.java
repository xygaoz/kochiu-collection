package com.keem.kochiu.collection.service;

import com.github.pagehelper.PageInfo;
import com.keem.kochiu.collection.data.bo.PageBo;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.data.vo.PageVo;
import com.keem.kochiu.collection.data.vo.ResourceVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.enums.SaveTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserResourceRepository;
import com.keem.kochiu.collection.service.strategy.ResourceStrategyFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class UserResourceService {

    private final ResourceStrategyFactory resourceStrategyFactory;
    private final SysUserRepository userRepository;
    private final UserResourceRepository resourceRepository;

    public UserResourceService(ResourceStrategyFactory resourceStrategyFactory,
                           SysUserRepository userRepository,
                           UserResourceRepository resourceRepository) {
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    /**
     * 通过策略保存文件
     * @param uploadBo
     * @return
     * @throws CollectionException
     */
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto) throws CollectionException {

        Integer userId = userDto != null ? userDto.getUserId() : null;
        if(userId == null){
            throw new CollectionException("非法请求。");
        }

        SysUser user = userRepository.getById(userId);
        if (user == null) {
            throw new CollectionException("非法请求。");
        }
        return resourceStrategyFactory.getStrategy(user.getStrategy())
                .saveFile(uploadBo, userDto);
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
                                                String contextPath, int cateSno,
                                                PageBo pageBo) throws CollectionException {

        Integer userId = userDto != null ? userDto.getUserId() : null;
        if(userId == null){
            throw new CollectionException("非法请求。");
        }

        SysUser user = userRepository.getById(userId);
        if (user == null) {
            throw new CollectionException("非法请求。");
        }

        PageInfo<UserResource> resourceList = resourceRepository.getResourceList(userId, cateSno, pageBo);
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

                        return ResourceVo.builder()
                                .resourceUrl(buildResourceUrl(user, resource, contextPath))
                                .thumbnailUrl(contextPath + "/" + resource.getResourceId() + "/" + resource.getThumbUrl().replace("/" + user.getUserCode() + "/", ""))
                                .title(resource.getTitle())
                                .description(resource.getDescription())
                                .sourceFileName(resource.getSourceFileName())
                                .width(width)
                                .height(height)
                                .fileType(FileTypeEnum.getByValue(resource.getFileExt()).getDesc())
                                .size(resource.getSize())
                                .resolutionRatio(resource.getResolutionRatio())
                                .createTime(resource.getCreateTime())
                                .updateTime(resource.getUpdateTime())
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
     * @param contextPath
     * @return
     */
    private String buildResourceUrl(SysUser user, UserResource resource, String contextPath){

        if(SaveTypeEnum.getByCode(resource.getSaveType()) != SaveTypeEnum.NETWORK){
            return contextPath + "/" + resource.getResourceId() + "/" + resource.getResourceUrl().replace("/" + user.getUserCode() + "/", "");
        }
        return resource.getThumbUrl();
    }
}

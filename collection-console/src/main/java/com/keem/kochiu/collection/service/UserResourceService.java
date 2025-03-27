package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.UserResourceRepository;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.service.strategy.ResourceStrategyFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public FileVo saveFile(UploadBo uploadBo) throws CollectionException {

        Integer userId = CheckPermitAspect.USER_INFO.get() != null ? CheckPermitAspect.USER_INFO.get().getUserId() : null;
        if(userId == null){
            throw new CollectionException("非法请求。");
        }

        SysUser user = userRepository.getById(userId);
        if (user == null) {
            throw new CollectionException("非法请求。");
        }
        return resourceStrategyFactory.getStrategy(user.getStrategy()).saveFile(uploadBo);
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
}

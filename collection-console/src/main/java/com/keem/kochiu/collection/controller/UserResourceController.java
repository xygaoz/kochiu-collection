package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.UserResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class UserResourceController {

    private final UserResourceService resourceService;

    public UserResourceController(UserResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @CheckPermit(on = {PermitEnum.UI, PermitEnum.API})
    @PostMapping("/api/v1/upload")
    public DefaultResult<FileVo> upload(@Valid UploadBo uploadBo) throws CollectionException {

        return DefaultResult.ok(resourceService.saveFile(uploadBo, CheckPermitAspect.USER_INFO.get()));
    }

    @GetMapping("/{resourceId}/**")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable int resourceId){

        resourceService.download(request, response, resourceId);
    }
}

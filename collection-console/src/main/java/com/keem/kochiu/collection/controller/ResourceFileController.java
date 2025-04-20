package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.BatchImportBo;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.ResourceFileService;
import com.keem.kochiu.collection.service.UserResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
public class ResourceFileController {

    private final String RESOURCE_PATH = PUBLIC_URL + "/resource";
    private final ResourceFileService resourceFileService;

    public ResourceFileController(ResourceFileService resourceFileService) {
        this.resourceFileService = resourceFileService;
    }


    @CheckPermit(on = {PermitEnum.UI, PermitEnum.API})
    @PostMapping(RESOURCE_PATH + "/upload")
    public DefaultResult<FileVo> upload(@Valid UploadBo uploadBo) throws CollectionException {

        return DefaultResult.ok(resourceFileService.saveFile(uploadBo, CheckPermitAspect.USER_INFO.get()));
    }

    @RequestMapping("/resource/{resourceId}/**")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable Long resourceId){

        resourceFileService.download(request, response, resourceId);
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/batchImport")
    public ResponseEntity<String> batchImport(BatchImportBo request) {
        String taskId = UUID.randomUUID().toString();
        CompletableFuture.runAsync(() -> {
            try {
                resourceFileService.batchImport(taskId, CheckPermitAspect.USER_INFO.get(), request);
            } catch (CollectionException e) {
                throw new RuntimeException(e);
            }
        });
        return ResponseEntity.ok(taskId);
    }

}
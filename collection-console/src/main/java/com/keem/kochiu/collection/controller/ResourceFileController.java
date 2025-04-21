package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.BatchImportBo;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.handler.ImportProgressWebSocketHandler;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.ImportTaskService;
import com.keem.kochiu.collection.service.ResourceFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@Slf4j
@RestController
public class ResourceFileController {

    private final String RESOURCE_PATH = PUBLIC_URL + "/resource";
    private final ResourceFileService resourceFileService;
    private final ImportTaskService taskService;

    public ResourceFileController(ResourceFileService resourceFileService,
                                  ImportTaskService taskService) {
        this.resourceFileService = resourceFileService;
        this.taskService = taskService;
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
    @PostMapping(RESOURCE_PATH + "/startBatchImport")
    public DefaultResult<String> startBatchImport(BatchImportBo request) {
        String taskId = UUID.randomUUID().toString();
        UserDto userDto = CheckPermitAspect.USER_INFO.get();

        // 通过 ImportTaskService 提交任务
        taskService.submitTask(() -> {
            try {
                resourceFileService.batchImport(taskId, userDto, request);
            } catch (CollectionException e) {
                log.error("批量导入失败", e);
                ImportProgressWebSocketHandler.sendProgress(taskId,
                        new ImportProgressWebSocketHandler.ImportProgress(0, 100, "", "error", e.getMessage())
                );
            }
        });

        return DefaultResult.ok(taskId);
    }

    @GetMapping(RESOURCE_PATH + "/cancelImport/{taskId}")
    public DefaultResult<String> cancelBatchImport(@PathVariable String taskId) {
        boolean success = taskService.cancelTask(taskId);
        if (success) {
            ImportProgressWebSocketHandler.sendCancelled(taskId); // 通知前端
            return DefaultResult.ok("取消成功");
        }
        return DefaultResult.fail("任务不存在或已完成");
    }
}
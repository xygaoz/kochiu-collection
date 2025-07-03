package com.kochiu.collection.controller;

import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.bo.BatchImportBo;
import com.kochiu.collection.data.bo.UploadBo;
import com.kochiu.collection.data.dto.ChunkUploadDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.FileVo;
import com.kochiu.collection.enums.PermitEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.handler.ImportProgressWebSocketHandler;
import com.kochiu.collection.service.CheckPermitAspect;
import com.kochiu.collection.service.ImportTaskService;
import com.kochiu.collection.service.ResourceFileService;
import com.kochiu.collection.service.file.FileStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.kochiu.collection.Constant.PUBLIC_URL;

@Slf4j
@RestController
public class ResourceFileController {

    private final String RESOURCE_PATH = PUBLIC_URL + "/resource";
    private final ResourceFileService resourceFileService;
    private final ImportTaskService taskService;
    private final FileStrategyFactory fileStrategyFactory;
    private final Executor asyncExecutor;

    public ResourceFileController(ResourceFileService resourceFileService,
                                  ImportTaskService taskService,
                                  FileStrategyFactory fileStrategyFactory,
                                  Executor asyncExecutor) {
        this.resourceFileService = resourceFileService;
        this.taskService = taskService;
        this.fileStrategyFactory = fileStrategyFactory;
        this.asyncExecutor = asyncExecutor;
    }

    @CheckPermit(on = {PermitEnum.UI, PermitEnum.API})
    @PostMapping(RESOURCE_PATH + "/check-file-exist")
    public DefaultResult<Boolean> checkFileExist(@NotNull String md5) throws CollectionException {
        UserDto userDto = CheckPermitAspect.USER_INFO.get();
        return DefaultResult.ok(resourceFileService.checkFileExist(userDto, md5));
    }

    @CheckPermit(on = {PermitEnum.UI, PermitEnum.API})
    @PostMapping(RESOURCE_PATH + "/upload")
    public DefaultResult<FileVo> upload(@Valid UploadBo uploadBo) throws CollectionException {
        // 立即返回上传成功响应，不等待缩略图生成
        UserDto userDto = CheckPermitAspect.USER_INFO.get();
        FileVo fileVo = resourceFileService.saveFile(uploadBo, userDto);

        log.debug("保存文件成功 - 文件名: {}, 文件大小: {}", fileVo.getUrl(), fileVo.getSize());
        // 异步生成缩略图
        CompletableFuture.runAsync(() -> {
            // 添加超时控制
            try {
                log.debug("开始生成缩略图 - 文件ID: {}", fileVo.getResourceId());
                resourceFileService.generateThumbnail(userDto, fileVo.getResourceId());
            } catch (Exception e) {
                log.error("缩略图生成失败", e);
            }
        }, asyncExecutor);
        log.debug("上传成功 - 文件ID: {}, 文件名: {}, 文件大小: {}", fileVo.getResourceId(), fileVo.getUrl(), fileVo.getSize());

        return DefaultResult.ok(fileVo);
    }

    @RequestMapping("/resource/{resourceId}/**")
    public ResponseEntity<Resource> downloadResource(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable Long resourceId,
            @RequestHeader HttpHeaders headers) {

        return resourceFileService.downloadResource(request, response, headers.getRange(), resourceId);
    }

    @CheckPermit
    @PostMapping(RESOURCE_PATH + "/startBatchImport")
    public DefaultResult<String> startBatchImport(BatchImportBo request) {
        String taskId = UUID.randomUUID().toString();
        UserDto userDto = CheckPermitAspect.USER_INFO.get();

        // 检查用户是否已有任务在执行
        if (taskService.hasRunningTask(userDto.getUserId())) {
            return DefaultResult.fail("您已有任务在执行中，请等待完成或取消当前任务");
        }

        try {
            resourceFileService.beforeImport(userDto, request);
        } catch (CollectionException e) {
            return DefaultResult.fail(e.getMessage());
        }

        // 通过 ImportTaskService 提交任务
        taskService.submitTask(userDto.getUserId(), taskId, () -> {
            try {
                resourceFileService.batchImport(taskId, userDto, request);
            } catch (CollectionException e) {
                log.error("批量导入失败", e);
                ImportProgressWebSocketHandler.sendProgress(taskId,
                        new ImportProgressWebSocketHandler.ImportProgress(-1, -1, -1, -1, "", "error", e.getMessage())
                );
            }
        });

        return DefaultResult.ok(taskId);
    }

    @CheckPermit
    @GetMapping(RESOURCE_PATH + "/cancelImport/{taskId}")
    public DefaultResult<Boolean> cancelBatchImport(@PathVariable String taskId) {
        boolean success = taskService.cancelTask(taskId);
        if (success) {
            ImportProgressWebSocketHandler.sendCancelled(taskId); // 通知前端
            return DefaultResult.ok(true);
        }
        return DefaultResult.fail("任务不存在或已完成");
    }

    @CheckPermit
    @GetMapping(RESOURCE_PATH + "/allowedTypes")
    public DefaultResult<Set<String>> getAllowedTypes() {
        return DefaultResult.ok(fileStrategyFactory.getAllowedTypes());
    }

    // 分片上传接口
    @CheckPermit(on = {PermitEnum.UI, PermitEnum.API})
    @PostMapping(RESOURCE_PATH + "/upload-chunk")
    public DefaultResult<Boolean> uploadChunk(ChunkUploadDto chunkDTO) {
        try {
            // 检查分片是否已上传
            if (resourceFileService.checkChunk(chunkDTO.getFileId(), chunkDTO.getChunkIndex())) {
                return DefaultResult.ok(true);
            }
            log.debug("开始上传分片 - 文件ID: {}, 分片索引: {}", chunkDTO.getFileId(), chunkDTO.getChunkIndex());

            // 存储分片
            resourceFileService.storeChunk(chunkDTO);
            return DefaultResult.ok(true);
        } catch (Exception e) {
            return DefaultResult.fail(e.getMessage());
        }
    }

    @CheckPermit(on = {PermitEnum.UI, PermitEnum.API})
    @PostMapping(RESOURCE_PATH + "/check-chunks")
    public DefaultResult<boolean[]> checkChunks(
            @RequestParam String fileId,
            @RequestParam Integer totalChunks) {

        boolean[] chunkStatus = new boolean[totalChunks];
        for (int i = 0; i < totalChunks; i++) {
            chunkStatus[i] = resourceFileService.checkChunk(fileId, i);
        }
        log.debug("分片检查完成 - 文件ID: {}, 总分片数: {}", fileId, totalChunks);

        return DefaultResult.ok(chunkStatus);
    }

    // 合并分片接口
    @CheckPermit(on = {PermitEnum.UI, PermitEnum.API})
    @PostMapping(RESOURCE_PATH + "/merge-chunks")
    public DefaultResult<Boolean> mergeChunks(ChunkUploadDto chunkDTO) {
        try {
            log.debug("开始合并分片 - 文件ID: {}, 文件名: {}", chunkDTO.getFileId(), chunkDTO.getOriginalName());
            File mergedFile = resourceFileService.mergeChunks(
                    chunkDTO.getFileId()
            );

            UserDto userDto = CheckPermitAspect.USER_INFO.get();
            FileVo fileVo = resourceFileService.saveFile(new FileSystemResource(mergedFile),
                    chunkDTO.getCategoryId(),
                    chunkDTO.getCataId(),
                    chunkDTO.getOverwrite(),
                    chunkDTO.getAutoCreate(),
                    chunkDTO.getOriginalName(),
                    userDto);

            log.debug("保存文件成功 - 文件名: {}, 文件大小: {}", fileVo.getUrl(), fileVo.getSize());
            // 异步生成缩略图
            CompletableFuture.runAsync(() -> {
                // 添加超时控制
                try {
                    log.debug("开始生成缩略图 - 文件ID: {}", fileVo.getResourceId());
                    resourceFileService.generateThumbnail(userDto, fileVo.getResourceId());
                } catch (Exception e) {
                    log.error("缩略图生成失败", e);
                }
            }, asyncExecutor);
            log.debug("上传成功 - 文件ID: {}, 文件名: {}, 文件大小: {}", fileVo.getResourceId(), fileVo.getUrl(), fileVo.getSize());
            Files.deleteIfExists(mergedFile.toPath());

            return DefaultResult.ok(true);
        } catch (Exception e) {
            log.error("上传失败", e);
            return DefaultResult.fail(e.getMessage());
        }
    }

    // 重建缩略图接口
    @CheckPermit
    @GetMapping(RESOURCE_PATH + "/rebuild-thumb/{resourceId}")
    public DefaultResult<Boolean> rebuildThumbnail(@PathVariable Long resourceId) throws CollectionException {

        UserDto userDto = CheckPermitAspect.USER_INFO.get();
        resourceFileService.generateThumbnail(userDto, resourceId);
        return DefaultResult.ok(true);
    }
}
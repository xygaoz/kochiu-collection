package com.kochiu.collection.service.store;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.repository.UserResourceRepository;
import com.kochiu.collection.service.file.FileStrategy;
import com.kochiu.collection.service.file.FileStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.*;

import static com.kochiu.collection.util.SysUtil.tidyPath;

/**
 * 缩略图服务
 */
@Slf4j
@Service
public class ThumbnailService {

    private final FileStrategyFactory fileStrategyFactory;
    private final UserResourceRepository resourceRepository;
    private final Executor asyncExecutor;
    private static final ConcurrentMap<String, Boolean> processingFiles = new ConcurrentHashMap<>();

    public ThumbnailService(FileStrategyFactory fileStrategyFactory,
                            UserResourceRepository resourceRepository,
                            @Qualifier("taskExecutor") Executor asyncExecutor) {
        this.fileStrategyFactory = fileStrategyFactory;
        this.resourceRepository = resourceRepository;
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * 异步生成缩略图（增强版）
     */
    public void asyncCreateThumbnail(ResourceDto resourceDto, FileType fileType, String filePath,
                                     String thumbFilePath, String thumbUrl) {
        // 检查文件是否正在处理中
        if (processingFiles.putIfAbsent(filePath, true) != null) {
            log.debug("文件正在处理中，跳过重复处理 - 资源ID: {}, 文件: {}",
                    resourceDto.getResourceId(), filePath);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                createThumbnailWithTimeout(resourceDto, fileType, filePath, thumbFilePath, thumbUrl);
            } catch (Exception e) {
                log.error("缩略图生成失败 - 资源ID: {}, 文件: {}",
                        resourceDto.getResourceId(), filePath, e);
            } finally {
                // 处理完成后从map中移除
                processingFiles.remove(filePath);
            }
        }, asyncExecutor).exceptionally(ex -> {
            log.error("缩略图生成任务异常 - 资源ID: {}, 文件: {}",
                    resourceDto.getResourceId(), filePath, ex);
            // 异常情况下也要从map中移除
            processingFiles.remove(filePath);
            return null;
        });
    }

    /**
     * 带超时控制的缩略图生成
     */
    private void createThumbnailWithTimeout(ResourceDto resourceDto, FileType fileType, String filePath,
                                            String thumbFilePath, String thumbUrl) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                internalCreateThumbnail(resourceDto, fileType, filePath, thumbFilePath, thumbUrl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                executor.shutdown();
            }
        });

        try {
            // 设置超时时间为5分钟
            future.get(10, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("缩略图生成超时 - 资源ID: {}, 文件: {}",
                    resourceDto.getResourceId(), filePath);
        } catch (Exception e) {
            log.error("缩略图生成异常 - 资源ID: {}, 文件: {}",
                    resourceDto.getResourceId(), filePath, e);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * 实际缩略图生成逻辑
     */
    private void internalCreateThumbnail(ResourceDto resourceDto, FileType fileType, String filePath,
                                         String thumbFilePath, String thumbUrl) throws Exception {
        if(thumbFilePath == null) {
            thumbFilePath = filePath.replace("." + resourceDto.getFileExt(), "_thumb.png");
        }
        if(thumbUrl == null) {
            thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");
        }

        FileStrategy fileStrategy = fileStrategyFactory.getStrategy(fileType);
        try {
            if (fileType.thumb()) {
                // 大文件处理前检查可用内存
                checkMemoryForLargeFile(filePath);

                fileStrategy.createThumbnail(new File(filePath), thumbFilePath, thumbUrl, fileType, resourceDto);
            } else {
                fileStrategy.defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
            }

            updateResourceInfo(resourceDto);
        } catch (OutOfMemoryError e) {
            log.error("内存不足无法生成缩略图 - 资源ID: {}, 文件大小: {} MB",
                    resourceDto.getResourceId(), new File(filePath).length() / (1024 * 1024));
            throw e;
        }
    }

    /**
     * 更新资源信息
     */
    private void updateResourceInfo(ResourceDto resourceDto) {
        UserResource userResource = resourceRepository.getById(resourceDto.getResourceId());
        if (userResource != null) {
            userResource.setThumbUrl(tidyPath(resourceDto.getThumbUrl()));
            userResource.setThumbRatio(resourceDto.getThumbRatio());
            userResource.setPreviewUrl(tidyPath(resourceDto.getPreviewUrl()));
            resourceRepository.updateById(userResource);
        }
    }


    /**
     * 检查大文件处理前的内存情况
     */
    private void checkMemoryForLargeFile(String filePath) {
        long fileSize = new File(filePath).length();
        if (fileSize > 100 * 1024 * 1024) { // 大于100MB的文件
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory();
            long totalMemory = runtime.totalMemory();
            long maxMemory = runtime.maxMemory();

            log.info("处理大文件前内存状态 - 文件大小: {} MB, 空闲内存: {} MB, 已分配内存: {} MB, 最大内存: {} MB",
                    fileSize / (1024 * 1024),
                    freeMemory / (1024 * 1024),
                    totalMemory / (1024 * 1024),
                    maxMemory / (1024 * 1024));

            if (fileSize > freeMemory * 0.5) {
                log.warn("可用内存可能不足处理此大文件");
                // 尝试释放内存
                System.gc();
            }
        }
    }
}

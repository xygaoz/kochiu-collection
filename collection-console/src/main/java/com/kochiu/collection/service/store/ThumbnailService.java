package com.kochiu.collection.service.store;

import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.repository.UserResourceRepository;
import com.kochiu.collection.service.file.FileStrategy;
import com.kochiu.collection.service.file.FileStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.File;

@EnableAsync
@Slf4j
@Service
public class ThumbnailService {

    private final FileStrategyFactory fileStrategyFactory;
    private final UserResourceRepository resourceRepository;

    public ThumbnailService(FileStrategyFactory fileStrategyFactory,
                            UserResourceRepository resourceRepository) {
        this.fileStrategyFactory = fileStrategyFactory;
        this.resourceRepository = resourceRepository;
    }

    /**
     * 异步生成缩略图
     */
    @Async
    public void asyncCreateThumbnail(ResourceDto resourceDto, FileTypeEnum fileType, String filePath) {

        //判断文件是否需要生成缩略图
        String thumbFilePath = filePath.replace("." + resourceDto.getFileExt(), "_thumb.png");
        String thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");

        FileStrategy fileStrategy = fileStrategyFactory.getStrategy(fileType);
        try {
            if(fileType.isThumb()) {
                fileStrategy.createThumbnail(new File(filePath), thumbFilePath, thumbUrl, fileType, resourceDto);
            }
            else{
                fileStrategy.defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
            }

            //更新资源信息
            UserResource userResource = resourceRepository.getById(resourceDto.getResourceId());
            if(userResource != null){
                userResource.setThumbUrl(resourceDto.getThumbUrl());
                userResource.setThumbRatio(resourceDto.getThumbRatio());
                userResource.setPreviewUrl(resourceDto.getPreviewUrl());
                resourceRepository.updateById(userResource);
            }
        } catch (Exception e) {
            log.error("缩略图生成失败", e);
        }
    }

}

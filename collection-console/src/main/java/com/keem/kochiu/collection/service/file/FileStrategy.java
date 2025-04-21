package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;

import java.io.File;

public interface FileStrategy {

    String createThumbnail(File file, String thumbFilePath,
                           String thumbUrl,
                           FileTypeEnum fileType,
                           ResourceDto resourceDto) throws Exception;
}

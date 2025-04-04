package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;

public interface FileStrategy {

    String createThumbnail(String filePath, String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception;
}

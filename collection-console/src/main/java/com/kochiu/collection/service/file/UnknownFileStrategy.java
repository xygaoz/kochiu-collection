package com.kochiu.collection.service.file;

import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.FileTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service("unknown")
public class UnknownFileStrategy implements FileStrategy{

    @Override
    public String createThumbnail(File filePath,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
    }
}

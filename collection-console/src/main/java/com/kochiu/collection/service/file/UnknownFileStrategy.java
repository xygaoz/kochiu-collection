package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service("unknown")
@FileType
public class UnknownFileStrategy implements FileStrategy{

    @Override
    public String createThumbnail(File filePath,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileType fileType,
                                  ResourceDto resourceDto) throws Exception {

        return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
    }
}

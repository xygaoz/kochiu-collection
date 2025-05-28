package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ResourceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service("mp3")
@FileType(mimeType = "audio/mpeg", desc = ResourceTypeEnum.AUDIO)
public class Mp3FileStrategy implements FileStrategy{

    @Override
    public String createThumbnail(File file,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileType fileType,
                                  ResourceDto resourceDto) throws Exception {
        return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
    }
}

package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("flac")
@FileType(mimeType = "audio/flac", desc = ResourceTypeEnum.AUDIO)
public class FlacFileStrategy extends Mp3FileStrategy{
}

package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("wav")
@FileType(mimeType = "audio/wav", desc = ResourceTypeEnum.AUDIO)
public class WavFileStrategy extends Mp3FileStrategy{
}

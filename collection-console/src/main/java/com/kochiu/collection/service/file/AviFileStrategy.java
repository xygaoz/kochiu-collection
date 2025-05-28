package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("avi")
@FileType(thumb = true, mimeType = "video/x-msvideo", desc = ResourceTypeEnum.VIDEO)
public class AviFileStrategy extends Mp4FileStrategy{

}

package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("mov")
@FileType(thumb = true, mimeType = "video/quicktime", desc = ResourceTypeEnum.VIDEO)
public class MovFileStrategy extends Mp4FileStrategy{

}

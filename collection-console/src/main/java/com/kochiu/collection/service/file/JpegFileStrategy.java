package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("jpeg")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/jpeg", desc = ResourceTypeEnum.IMAGE)
public class JpegFileStrategy extends JpgFileStrategy{
}

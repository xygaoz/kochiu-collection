package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("webp")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/webp", desc = ResourceTypeEnum.IMAGE)
public class WebpFileStrategy extends JpgFileStrategy{
}

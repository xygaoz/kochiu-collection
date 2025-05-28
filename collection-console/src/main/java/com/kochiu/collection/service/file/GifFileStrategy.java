package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("gif")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/gif", desc = ResourceTypeEnum.IMAGE)
public class GifFileStrategy extends JpgFileStrategy{
}

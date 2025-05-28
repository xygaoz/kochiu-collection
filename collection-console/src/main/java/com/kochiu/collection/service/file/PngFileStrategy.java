package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("png")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/png", desc = ResourceTypeEnum.IMAGE)
public class PngFileStrategy extends JpgFileStrategy{
}

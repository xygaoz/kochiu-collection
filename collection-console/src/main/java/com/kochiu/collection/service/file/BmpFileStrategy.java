package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("bmp")
@FileType(thumb = true, resolutionRatio = true, mimeType = "images/bmp", desc = ResourceTypeEnum.IMAGE)
public class BmpFileStrategy extends JpgFileStrategy{
}

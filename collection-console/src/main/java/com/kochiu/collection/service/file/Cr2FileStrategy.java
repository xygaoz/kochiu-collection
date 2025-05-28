package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("cr2")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-canon-cr2", desc = ResourceTypeEnum.IMAGE)
public class Cr2FileStrategy extends DngFileStrategy{
}

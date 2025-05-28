package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

@Service("psd")
@FileType(thumb = true, mimeType = "image/vnd.adobe.photoshop", desc = ResourceTypeEnum.IMAGE)
public class PsdFileStrategy extends TifFileStrategy{
}

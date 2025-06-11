package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("mov")
@FileType(thumb = true, mimeType = "video/quicktime", desc = ResourceTypeEnum.VIDEO)
public class MovFileStrategy extends Mp4FileStrategy{

    public MovFileStrategy(CollectionProperties properties) {
        super(properties);
    }
}

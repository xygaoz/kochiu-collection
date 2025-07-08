package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("mts")
@FileType(thumb = true, mimeType = "video/mp2t", desc = ResourceTypeEnum.VIDEO)
public class MtsFileStrategy extends Mp4FileStrategy{

    public MtsFileStrategy(CollectionProperties properties) {
        super(properties);
    }
}

package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("cr2")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-canon-cr2", desc = ResourceTypeEnum.IMAGE)
public class Cr2FileStrategy extends DngFileStrategy{

    public Cr2FileStrategy(CollectionProperties collectionProperties) {
        super(collectionProperties);
    }

    @Override
    protected String getPreviewExtension() {
        return ".cr2.png";
    }
}

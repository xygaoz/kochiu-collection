package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("nef")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-nikon-nef", desc = ResourceTypeEnum.IMAGE)
public class NefFileStrategy extends DngFileStrategy{

    public NefFileStrategy(CollectionProperties collectionProperties) {
        super(collectionProperties);
    }

    @Override
    protected String getPreviewExtension() {
        return ".nef.png";
    }
}

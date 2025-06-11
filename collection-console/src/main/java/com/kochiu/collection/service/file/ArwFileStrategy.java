package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("arw")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-sony-arw", desc = ResourceTypeEnum.IMAGE)
public class ArwFileStrategy extends DngFileStrategy{

    public ArwFileStrategy(CollectionProperties collectionProperties) {
        super(collectionProperties);
    }

    @Override
    protected String getPreviewExtension() {
        return ".arw.png";
    }

}

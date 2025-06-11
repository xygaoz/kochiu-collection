package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("psb")
@FileType(thumb = true, mimeType = "application/x-photoshop", desc = ResourceTypeEnum.IMAGE)
public class PsbFileStrategy extends PsdFileStrategy{

    public PsbFileStrategy(CollectionProperties collectionProperties) {
        super(collectionProperties);
    }

    @Override
    protected String getPreviewExtension() {
        return ".pdb.png";
    }
}

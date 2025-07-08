package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("pptx")
@FileType(thumb = true, mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation", desc = ResourceTypeEnum.DOCUMENT)
public class PptxFileStrategy extends PptFileStrategy{

    public PptxFileStrategy(CollectionProperties properties,
                            PdfFileStrategy pdfFileStrategy) {
        super(properties, pdfFileStrategy);
    }
}

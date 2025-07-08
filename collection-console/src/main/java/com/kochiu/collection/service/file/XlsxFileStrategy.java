package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("xlsx")
@FileType(thumb = true, mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", desc = ResourceTypeEnum.DOCUMENT)
public class XlsxFileStrategy extends XlsFileStrategy{

    public XlsxFileStrategy(CollectionProperties properties,
                            PdfFileStrategy pdfFileStrategy) {
        super(properties, pdfFileStrategy);
    }
}

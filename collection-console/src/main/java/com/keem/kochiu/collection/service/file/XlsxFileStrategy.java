package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;

@Service("xlsx")
public class XlsxFileStrategy extends XlsFileStrategy{

    public XlsxFileStrategy(CollectionProperties properties,
                            PdfFileStrategy pdfFileStrategy) {
        super(properties, pdfFileStrategy);
    }
}

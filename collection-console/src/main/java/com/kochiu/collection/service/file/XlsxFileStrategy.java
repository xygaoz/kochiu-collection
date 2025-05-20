package com.kochiu.collection.service.file;

import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("xlsx")
public class XlsxFileStrategy extends XlsFileStrategy{

    public XlsxFileStrategy(CollectionProperties properties,
                            PdfFileStrategy pdfFileStrategy) {
        super(properties, pdfFileStrategy);
    }
}

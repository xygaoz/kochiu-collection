package com.kochiu.collection.service.file;

import com.kochiu.collection.properties.CollectionProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("pptx")
public class PptxFileStrategy extends PptFileStrategy{

    public PptxFileStrategy(CollectionProperties properties,
                            PdfFileStrategy pdfFileStrategy) {
        super(properties, pdfFileStrategy);
    }
}

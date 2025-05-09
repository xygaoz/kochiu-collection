package com.kochiu.collection.service.file;

import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;

@Service("docx")
public class DocxFileStrategy extends DocFileStrategy{

    public DocxFileStrategy(CollectionProperties properties,
                            PdfFileStrategy pdfFileStrategy,
                            RestTemplate restTemplate) {
        super(properties, pdfFileStrategy, restTemplate);
    }

    /**
     * 生成缩略图
     *
     * @param wordFile
     * @param thumbFilePath
     * @param fileType
     * @param resourceDto
     * @return
     * @throws Exception
     */
    @Override
    public String createThumbnail(File wordFile,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        String pdfPath = thumbFilePath.substring(0, thumbFilePath.lastIndexOf("_thumb.png")) + ".pdf";

        // Step 1: Convert Word to HTML
        if (properties.getJodconverter().isEnabled()) {
            convertDocToPdfOfJodconverter(wordFile, pdfPath);
        }
        else {
            convertDocxToPdf(wordFile, pdfPath);
        }

        // Step 3: Convert PDF first page to image
        String thumbRatio = pdfFileStrategy.createThumbnail(new File(pdfPath), thumbFilePath, thumbUrl, fileType, resourceDto);
        resourceDto.setThumbRatio(thumbRatio);
        resourceDto.setThumbUrl(thumbUrl);
        resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));

        return thumbRatio;
    }

    private static void convertDocxToPdf(File wordFile, String outputPath) throws Exception {
        // 1. 加载 Word 文档
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(wordFile);

        // 2. 配置输出流
        FileOutputStream outputStream = new FileOutputStream(outputPath);

        // 3. 转换为 PDF
        Docx4J.toPDF(wordMLPackage, outputStream);

        // 4. 关闭资源
        outputStream.close();
        System.out.println("转换成功！");
    }
}

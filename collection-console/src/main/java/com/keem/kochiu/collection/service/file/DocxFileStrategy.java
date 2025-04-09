package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.properties.CollectionProperties;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;

@Service("docx")
public class DocxFileStrategy extends DocFileStrategy{

    public DocxFileStrategy(CollectionProperties properties,
                            PdfFileStrategy pdfFileStrategy) {
        super(properties, pdfFileStrategy);
    }

    /**
     * 生成缩略图
     *
     * @param filePath
     * @param thumbFilePath
     * @param fileType
     * @param resourceDto
     * @return
     * @throws Exception
     */
    @Override
    public String createThumbnail(String filePath,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        String pdfPath = filePath.replace(".docx", ".pdf");

        // Step 1: Convert Word to HTML
        if(properties.getOfficeHome() != null && new File(properties.getOfficeHome()).exists()){
            convertDocToPdfOfJodconverter(filePath, pdfPath);
        }
        else {
            convertDocxToPdf(filePath, pdfPath);
        }

        // Step 3: Convert PDF first page to image
        String thumbRatio = pdfFileStrategy.createThumbnail(pdfPath, thumbFilePath, thumbUrl, fileType, resourceDto);
        resourceDto.setThumbRatio(thumbRatio);
        resourceDto.setThumbUrl(thumbUrl);
        resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));

        return thumbRatio;
    }

    private static void convertDocxToPdf(String wordPath, String outputPath) throws Exception {
        // 1. 加载 Word 文档
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(wordPath));

        // 2. 配置输出流
        FileOutputStream outputStream = new FileOutputStream(outputPath);

        // 3. 转换为 PDF
        Docx4J.toPDF(wordMLPackage, outputStream);

        // 4. 关闭资源
        outputStream.close();
        System.out.println("转换成功！");
    }
}

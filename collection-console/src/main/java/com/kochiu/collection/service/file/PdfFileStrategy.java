package com.kochiu.collection.service.file;

import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.util.ImageUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;

@Service("pdf")
public class PdfFileStrategy implements FileStrategy{

    /**
     * 生成缩略图
     *
     * @param file
     * @param thumbFilePath
     * @param fileType
     * @param resourceDto
     * @return
     * @throws Exception
     */
    @Override
    public String createThumbnail(File file,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(0, 1.5f); // 1.5倍缩放提升清晰度

            // 生成缩略图
            resourceDto.setThumbRatio(ImageUtil.writeThumbnail(image, thumbFilePath));
            resourceDto.setThumbUrl(thumbUrl);

            return resourceDto.getThumbRatio();
        }
    }

}

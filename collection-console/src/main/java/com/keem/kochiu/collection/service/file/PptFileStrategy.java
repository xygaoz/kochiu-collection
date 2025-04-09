package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.filter.PagesSelectorFilter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service("ppt")
public class PptFileStrategy implements FileStrategy{

    protected final CollectionProperties properties;
    protected final PdfFileStrategy pdfFileStrategy;

    public PptFileStrategy(CollectionProperties properties, PdfFileStrategy pdfFileStrategy) {
        this.properties = properties;
        this.pdfFileStrategy = pdfFileStrategy;
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

        String thumbRatio;
        if (properties.getOfficeHome() != null && new File(properties.getOfficeHome()).exists()) {
            String pdfPath = filePath.replace(".pptx", ".pdf");
            pdfPath = pdfPath.replace(".ppt", ".pdf");

            OfficeManager officeManager = LocalOfficeManager.builder()
                    .officeHome(properties.getOfficeHome())
                    .install()
                    .build();

            try {
                officeManager.start();

                LocalConverter.builder()
                        .officeManager(officeManager)
                        .filterChain(
                                // 设置页码范围（如第1-3页）
                                new PagesSelectorFilter(1, 1)
                        )
                        .build()
                        .convert(new File(filePath))
                        .to(new File(pdfPath))
                        .execute();

                thumbRatio = pdfFileStrategy.createThumbnail(pdfPath, thumbFilePath, thumbUrl, fileType, resourceDto);
                resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));
            } finally {
                officeManager.stop();
            }
        }
        else{
            thumbRatio = convertPptFirstPage(filePath, thumbFilePath);
        }
        resourceDto.setThumbRatio(thumbRatio);
        resourceDto.setThumbUrl(thumbUrl);
        return thumbRatio;
    }

    public String convertPptFirstPage(String pptPath, String outputPath) throws IOException {
        try (SlideShow<?, ?> slideShow = new XMLSlideShow(new FileInputStream(pptPath))) {
            Dimension pageSize = slideShow.getPageSize();
            BufferedImage image = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            slideShow.getSlides().get(0).draw(graphics); // 绘制第一页
            graphics.dispose();

            // 生成缩略图
            return ImageUtil.writeThumbnail(image, outputPath);
        }
    }
}

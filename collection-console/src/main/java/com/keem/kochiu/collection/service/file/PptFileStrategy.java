package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.enums.JodconverterModeEnum;
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
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service("ppt")
public class PptFileStrategy extends OfficeFileStrategy implements FileStrategy{

    protected final CollectionProperties properties;
    protected final PdfFileStrategy pdfFileStrategy;

    public PptFileStrategy(CollectionProperties properties,
                           PdfFileStrategy pdfFileStrategy,
                           RestTemplate restTemplate) {
        super(properties, restTemplate);
        this.properties = properties;
        this.pdfFileStrategy = pdfFileStrategy;
    }

    /**
     * 生成缩略图
     *
     * @param pptFile
     * @param thumbFilePath
     * @param fileType
     * @param resourceDto
     * @return
     * @throws Exception
     */
    @Override
    public String createThumbnail(File pptFile,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        String thumbRatio;
        if (properties.getJodconverter().isEnabled()) {
            log.info("使用jodconverter转换ppt文件");
            if(properties.getJodconverter().getMode() == JodconverterModeEnum.LOCAL){
               if(properties.getJodconverter().getLocal().getOfficeHome() != null && new File(properties.getJodconverter().getLocal().getOfficeHome()).exists()){
                   log.info("使用jodconverter转换ppt文件为pdf文件");
                   String pdfPath = thumbFilePath.substring(0, thumbFilePath.lastIndexOf("_thumb.png")) + ".pdf";

                   OfficeManager officeManager = LocalOfficeManager.builder()
                           .officeHome(properties.getJodconverter().getLocal().getOfficeHome())
                           .install()
                           .build();

                   try {
                       officeManager.start();

                       LocalConverter.builder()
                               .officeManager(officeManager)
                               .filterChain(
                                       // 设置页码范围（如第1-3页）
                                       new PagesSelectorFilter(1, 2, 3)
                               )
                               .build()
                               .convert(pptFile)
                               .to(new File(pdfPath))
                               .execute();

                       thumbRatio = pdfFileStrategy.createThumbnail(new File(pdfPath), thumbFilePath, thumbUrl, fileType, resourceDto);
                       resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));
                   } finally {
                       officeManager.stop();
                   }
               }
               else{
                   thumbRatio = convertPptFirstPage(pptFile, thumbFilePath);
               }
            }
            else if(properties.getJodconverter().getMode() == JodconverterModeEnum.REMOTE) {
                log.info("使用jodconverter远程转换ppt文件为pdf文件");

                if(properties.getJodconverter().getRemote().getApiUrl() == null) {
                    thumbRatio = convertPptFirstPage(pptFile, thumbFilePath);
                }
                else {
                    try {
                        String pdfPath = thumbFilePath.substring(0, thumbFilePath.lastIndexOf("_thumb.png")) + ".pdf";
                        remoteConvertToPdf(pptFile, new File(pdfPath));

                        thumbRatio = pdfFileStrategy.createThumbnail(new File(pdfPath), thumbFilePath, thumbUrl, fileType, resourceDto);
                        resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));
                    } catch (Exception e) {
                        log.error("远程转换ppt文件为pdf文件失败", e);
                        thumbRatio = convertPptFirstPage(pptFile, thumbFilePath);
                    }
                }
            }
            else{
                thumbRatio = convertPptFirstPage(pptFile, thumbFilePath);
            }
        }
        else{
            thumbRatio = convertPptFirstPage(pptFile, thumbFilePath);
        }
        resourceDto.setThumbRatio(thumbRatio);
        resourceDto.setThumbUrl(thumbUrl);
        return thumbRatio;
    }

    public String convertPptFirstPage(File pptFile, String outputPath) throws IOException {
        try (SlideShow<?, ?> slideShow = new XMLSlideShow(new FileInputStream(pptFile))) {
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

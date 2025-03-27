package com.keem.kochiu.collection.util;

import cn.hutool.core.img.ImgUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.docx4j.Docx4J;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * 文档转图片
 */
@Slf4j
public class DocumentToImageConverter {

    static {
        try {
            // 精确注册字体名称（与 Word 文档中字体名称一致）
            PhysicalFonts.addPhysicalFonts("SimSun",
                    Objects.requireNonNull(DocumentToImageConverter.class.getResource("/fonts/simsun.ttc")).toURI()); // 或 "SimSun" 根据文档实际名称
            PhysicalFonts.addPhysicalFonts("宋体",
                    Objects.requireNonNull(DocumentToImageConverter.class.getResource("/fonts/simsun.ttc")).toURI()); // 或 "SimSun" 根据文档实际名称
            PhysicalFonts.addPhysicalFonts("Microsoft YaHei",
                    Objects.requireNonNull(DocumentToImageConverter.class.getResource("/fonts/msyh.ttc")).toURI());
            PhysicalFonts.addPhysicalFonts("楷体_GB2312",
                    Objects.requireNonNull(DocumentToImageConverter.class.getResource("/fonts/simkai.ttf")).toURI()); // 或 "SimSun" 根据文档实际名称
            PhysicalFonts.addPhysicalFonts("仿宋_GB2312",
                    Objects.requireNonNull(DocumentToImageConverter.class.getResource("/fonts/simfang.ttf")).toURI()); // 或 "SimSun" 根据文档实际名称
            PhysicalFonts.addPhysicalFonts("Calibri",
                    Objects.requireNonNull(DocumentToImageConverter.class.getResource("/fonts/calibri.ttf")).toURI()); // 或 "SimSun" 根据文档实际名称
            PhysicalFonts.addPhysicalFonts("Calibri Light",
                    Objects.requireNonNull(DocumentToImageConverter.class.getResource("/fonts/calibrili.ttf")).toURI()); // 或 "SimSun" 根据文档实际名称
        }
        catch (Exception e) {
            log.error("字体加载失败", e);
        }
    }

    // ---------- PDF 处理 ----------
    public static void convertPdfFirstPage(String pdfPath, String outputPath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(0, 1.5f); // 1.5倍缩放提升清晰度

            // 生成缩略图
            ImageUtil.writeThumbnail(image, outputPath);
        }
    }

    // ---------- PPT 处理 ----------
    public static void convertPptFirstPage(String pptPath, String outputPath) throws IOException {
        try (SlideShow<?, ?> slideShow = new XMLSlideShow(new FileInputStream(pptPath))) {
            Dimension pageSize = slideShow.getPageSize();
            BufferedImage image = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            slideShow.getSlides().get(0).draw(graphics); // 绘制第一页

            // 生成缩略图
            ImageUtil.writeThumbnail(image, outputPath);

            graphics.dispose();
        }
    }

    // ---------- word 处理 ----------
    public static void convertWordToImage(String wordPath, String outputPath) throws Exception {

        String pdfPath = wordPath.replace(".docx", ".pdf");
        pdfPath = pdfPath.replace(".doc", ".pdf");

        // Step 1: Convert Word to HTML
        convertWordToPdf(wordPath, pdfPath);

        // Step 3: Convert PDF first page to image
        convertPdfFirstPage(pdfPath, outputPath);

//        FileUtil.del(pdfPath);
    }

    private static void convertWordToPdf(String wordPath, String outputPath) throws Exception {
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

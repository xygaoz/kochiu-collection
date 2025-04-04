package com.keem.kochiu.collection.util;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.keem.kochiu.collection.properties.CollectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.Docx4J;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.filter.PagesSelectorFilter;
import org.jodconverter.local.office.LocalOfficeManager;

import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * 文档转图片
 */
@Slf4j
public class DocumentToImageConverter {

    static CollectionProperties properties = SpringBeanUtil.getBean(CollectionProperties.class);
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
    public static String convertPdfFirstPage(String pdfPath, String outputPath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(0, 1.5f); // 1.5倍缩放提升清晰度

            // 生成缩略图
            return ImageUtil.writeThumbnail(image, outputPath);
        }
    }

    /**
     * PPT 转图片
     * @param pptPath
     * @param outputPath
     * @return
     * @throws Exception
     */
    public static String convertPptToImage(String pptPath, String outputPath) throws Exception {

        if (properties.getOfficeHome() != null && new File(properties.getOfficeHome()).exists()) {
            String pdfPath = pptPath.replace(".pptx", ".pdf");
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
                        .convert(new File(pptPath))
                        .to(new File(pdfPath))
                        .execute();

                String thumbRatio = convertPdfFirstPage(pdfPath, outputPath);
                FileUtil.del(pdfPath);
                return thumbRatio;
            } finally {
                officeManager.stop();
            }
        }
        else{
            return convertPptFirstPage(pptPath, outputPath);
        }
    }

    // ---------- PPT 处理 ----------
    public static String convertPptFirstPage(String pptPath, String outputPath) throws IOException {
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

    // ---------- word 处理 ----------
    public static String convertWordToImage(String wordPath, String outputPath) throws Exception {

        String pdfPath = wordPath.replace(".docx", ".pdf");
        pdfPath = pdfPath.replace(".doc", ".pdf");

        // Step 1: Convert Word to HTML
        if(wordPath.endsWith(".docx")) {
            if(properties.getOfficeHome() != null && new File(properties.getOfficeHome()).exists()){
                convertDocToPdfOfJodconverter(wordPath, pdfPath);
            }
            else {
                convertDocxToPdf(wordPath, pdfPath);
            }
        }
        else if(wordPath.endsWith(".doc")) {
            if(properties.getOfficeHome() != null && new File(properties.getOfficeHome()).exists()){
                convertDocToPdfOfJodconverter(wordPath, pdfPath);
            }
            else {
                convertDocToPdf(wordPath, pdfPath);
            }
        }

        // Step 3: Convert PDF first page to image
        String thumbRatio = convertPdfFirstPage(pdfPath, outputPath);

        FileUtil.del(pdfPath);

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

    private static void convertDocToPdf(String wordPath, String outputPath) throws Exception{
        try (FileInputStream fis = new FileInputStream(wordPath);
             HWPFDocument doc = new HWPFDocument(fis)) {

            WordExtractor extractor = new WordExtractor(doc);
            String text = extractor.getText();

            // 创建 PDF
            Document pdfDoc = new Document();
            PdfWriter.getInstance(pdfDoc, new FileOutputStream(outputPath));
            pdfDoc.open();
            pdfDoc.add(new Paragraph(text));  // 仅提取文本，不保留格式
            pdfDoc.close();

            System.out.println("PDF 生成成功！");
        } catch (Exception e) {
            System.err.println("转换失败：" + e.getMessage());
        }
    }

    private static void convertDocToPdfOfJodconverter(String wordPath, String outputPath) throws Exception{
        // 启动 LibreOffice 服务
        OfficeManager officeManager = LocalOfficeManager.builder()
                .officeHome(properties.getOfficeHome())  // 修改为你的 LibreOffice 路径
                .install()
                .build();
        try {
            officeManager.start();
            LocalConverter.make()
                    .convert(new File(wordPath))
                    .to(new File(outputPath))
                    .execute();
            System.out.println("PDF 转换成功！");
        } catch (Exception e) {
            System.err.println("转换失败：" + e.getMessage());
        } finally {
            officeManager.stop();
        }
    }

    /**
     * 将 Excel 转换为图片
     * @param excelPath
     * @param outputPath
     * @return
     */
    public static String convertExcelToImage(String excelPath, String outputPath) throws Exception {
        if(properties.getOfficeHome() != null && new File(properties.getOfficeHome()).exists()) {
            String pdfPath = excelPath.replace(".xlsx", ".pdf");
            pdfPath = pdfPath.replace(".xls", ".pdf");

            convertExcelToPdfOfJodconverter(excelPath, pdfPath);

            String thumbRatio = convertPdfFirstPage(pdfPath, outputPath);
            FileUtil.del(pdfPath);
            return thumbRatio;
        }
        else{
            return convertExcelToImageOfDraw(excelPath, outputPath);
        }
    }

    /**
     * 将 Excel 转换为图片
     * @param excelPath
     * @param outputPath
     */
    private static String convertExcelToImageOfDraw(String excelPath, String outputPath){
        // A4 纸尺寸（300 DPI）
        final int A4_WIDTH = 2480;
        final int A4_HEIGHT = 3508;

        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook workbook = excelPath.endsWith(".xls") ? new HSSFWorkbook(fis) : new XSSFWorkbook(fis)) {

            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);

            // 计算图像的宽度和高度
            int width = 0;
            int height = 0;
            for (Row row : sheet) {
                height += 20; // 每行的高度
                for (Cell cell : row) {
                    width = Math.max(width, cell.getColumnIndex() * 100); // 每列的宽度
                }
            }

            // 限制在 A4 纸大小
            width = Math.min(width, A4_WIDTH);
            height = Math.min(height, A4_HEIGHT);

            // 创建图像缓冲区
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 绘制 Excel 内容
            int y = 0;
            for (Row row : sheet) {
                int x = 0;
                for (Cell cell : row) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(getCellValue(cell), x, y + 15);
                    x += 100; // 每列的宽度
                }
                y += 20; // 每行的高度
            }

            // 保存图像
            return ImageUtil.writeThumbnail(image, outputPath);
        } catch (IOException e) {
            log.error("转换失败", e);
        }

        return null;
    }

    private static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static void convertExcelToPdfOfJodconverter(String excelPath, String outputPath) throws Exception{
        // 启动 LibreOffice 服务
        OfficeManager officeManager = LocalOfficeManager.builder()
                .officeHome(properties.getOfficeHome())  // 修改为你的 LibreOffice 路径
                .install()
                .build();
        try {
            officeManager.start();
            LocalConverter.make()
                    .convert(new File(excelPath))
                    .to(new File(outputPath))
                    .execute();
            System.out.println("Excel 转换成功！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            officeManager.stop();
        }
    }

    /**
     * 将 TXT 文件转换为图片
     * @param txtPath
     * @param outputPath
     */
    public static String convertTxtToImage(String txtPath, String outputPath) {
        // A4 纸尺寸（300 DPI）
        final int A4_WIDTH = 2480;
        final int A4_HEIGHT = 3508;

        try {
            List<String> lines = Files.readAllLines(Paths.get(txtPath));

            // 计算图像的高度
            int height = Math.min(lines.size() * 20, A4_HEIGHT); // 每行的高度为20像素
            int width = A4_WIDTH; // 固定宽度为A4纸宽度

            // 创建图像缓冲区
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 绘制文本内容
            int y = 20; // 初始y坐标
            for (String line : lines) {
                if (y > height) break; // 如果超出高度则停止绘制
                g2d.setColor(Color.BLACK);
                g2d.drawString(line, 50, y); // 每行的x坐标为50像素
                y += 20; // 每行的高度为20像素
            }
            g2d.dispose();

            // 保存图像
            return ImageUtil.writeThumbnail(image, outputPath);
        } catch (IOException e) {
            log.error("转换失败", e);
        }
        return null;
    }
}

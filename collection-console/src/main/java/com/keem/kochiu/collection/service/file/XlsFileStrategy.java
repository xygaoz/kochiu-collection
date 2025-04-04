package com.keem.kochiu.collection.service.file;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Service("xls")
public class XlsFileStrategy implements FileStrategy{

    protected final CollectionProperties properties;
    protected final PdfFileStrategy pdfFileStrategy;

    public XlsFileStrategy(CollectionProperties properties, PdfFileStrategy pdfFileStrategy) {
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
        if(properties.getOfficeHome() != null && new File(properties.getOfficeHome()).exists()) {
            String pdfPath = filePath.replace(".xlsx", ".pdf");
            pdfPath = pdfPath.replace(".xls", ".pdf");

            convertExcelToPdfOfJodconverter(filePath, pdfPath);

            thumbRatio = pdfFileStrategy.createThumbnail(pdfPath, thumbFilePath, thumbUrl, fileType, resourceDto);

            FileUtil.del(pdfPath);
        }
        else{
            thumbRatio = convertExcelToImageOfDraw(filePath, thumbFilePath);
        }
        resourceDto.setThumbRatio(thumbRatio);
        resourceDto.setThumbUrl(thumbUrl);
        return thumbRatio;
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

    private void convertExcelToPdfOfJodconverter(String excelPath, String outputPath) throws Exception{
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
}

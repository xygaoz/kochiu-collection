package com.kochiu.collection.service.file;

import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.enums.JodconverterModeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service("xls")
public class XlsFileStrategy extends OfficeFileStrategy implements FileStrategy{

    protected final CollectionProperties properties;
    protected final PdfFileStrategy pdfFileStrategy;

    public XlsFileStrategy(CollectionProperties properties,
                           PdfFileStrategy pdfFileStrategy,
                           RestTemplate restTemplate) {
        super(properties, restTemplate);
        this.properties = properties;
        this.pdfFileStrategy = pdfFileStrategy;
    }

    /**
     * 生成缩略图
     *
     * @param excelFile
     * @param thumbFilePath
     * @param fileType
     * @param resourceDto
     * @return
     * @throws Exception
     */
    @Override
    public String createThumbnail(File excelFile,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        String thumbRatio;
        if (properties.getJodconverter().isEnabled()) {
            log.info("使用jodconverter转换ppt文件");
            String pdfPath = thumbFilePath.substring(0, thumbFilePath.lastIndexOf("_thumb.png")) + ".pdf";

            if(properties.getJodconverter().getMode() == JodconverterModeEnum.LOCAL) {
                if (properties.getJodconverter().getLocal().getOfficeHome() != null && new File(properties.getJodconverter().getLocal().getOfficeHome()).exists()) {
                    convertExcelToPdfOfJodconverter(excelFile, pdfPath);

                    thumbRatio = pdfFileStrategy.createThumbnail(new File(pdfPath), thumbFilePath, thumbUrl, fileType, resourceDto);
                    resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));
                } else {
                    thumbRatio = convertExcelToImageOfDraw(excelFile, thumbFilePath);
                }
            }
            else if(properties.getJodconverter().getMode() == JodconverterModeEnum.REMOTE){
                if(properties.getJodconverter().getRemote().getApiHost() == null) {
                    thumbRatio = convertExcelToImageOfDraw(excelFile, thumbFilePath);
                }
                else {
                    try {
                        remoteConvertToPdf(excelFile, new File(pdfPath));

                        thumbRatio = pdfFileStrategy.createThumbnail(new File(pdfPath), thumbFilePath, thumbUrl, fileType, resourceDto);
                        resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));
                    } catch (Exception e) {
                        log.error("远程转换excel文件为pdf文件失败", e);
                        thumbRatio = convertExcelToImageOfDraw(excelFile, thumbFilePath);
                    }
                }
            }
            else{
                thumbRatio = convertExcelToImageOfDraw(excelFile, thumbFilePath);
            }
        }
        else{
            thumbRatio = convertExcelToImageOfDraw(excelFile, thumbFilePath);
        }
        resourceDto.setThumbRatio(thumbRatio);
        resourceDto.setThumbUrl(thumbUrl);
        return thumbRatio;
    }

    /**
     * 将 Excel 转换为图片
     * @param excelFile
     * @param outputPath
     */
    private static String convertExcelToImageOfDraw(File excelFile, String outputPath){
        // A4 纸尺寸（300 DPI）
        final int A4_WIDTH = 2480;
        final int A4_HEIGHT = 3508;

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = excelFile.getName().endsWith(".xls") ? new HSSFWorkbook(fis) : new XSSFWorkbook(fis)) {

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

    private void convertExcelToPdfOfJodconverter(File excelFile, String outputPath) throws Exception{
        // 启动 LibreOffice 服务
        OfficeManager officeManager = LocalOfficeManager.builder()
                .officeHome(properties.getJodconverter().getLocal().getOfficeHome())  // 修改为你的 LibreOffice 路径
                .install()
                .build();
        try {
            officeManager.start();
            LocalConverter.make()
                    .convert(excelFile)
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

package com.kochiu.collection.service.file;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.enums.JodconverterModeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Slf4j
@Service("doc")
public class DocFileStrategy extends OfficeFileStrategy implements FileStrategy{

    protected final CollectionProperties properties;
    protected final PdfFileStrategy pdfFileStrategy;

    public DocFileStrategy(CollectionProperties properties,
                           PdfFileStrategy pdfFileStrategy,
                           RestTemplate restTemplate) {
        super(properties, restTemplate);
        this.properties = properties;
        this.pdfFileStrategy = pdfFileStrategy;
    }

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

        String pdfPath = thumbFilePath.substring(0, thumbFilePath.lastIndexOf("_thumb.png")) + ".pdf";

        // Step 1: Convert Word to HTML
        if (properties.getJodconverter().isEnabled()) {
            log.info("使用jodconverter转换ppt文件");
            convertDocToPdfOfJodconverter(file, pdfPath);
        }
        else {
            convertDocToPdf(file, pdfPath);
        }

        // Step 3: Convert PDF first page to image
        String thumbRatio = pdfFileStrategy.createThumbnail(new File(pdfPath), thumbFilePath, thumbUrl, fileType, resourceDto);
        resourceDto.setThumbRatio(thumbRatio);
        resourceDto.setThumbUrl(thumbUrl);

        resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".pdf"));

        return thumbRatio;
    }

    private void convertDocToPdf(File wordFile, String outputPath) throws Exception{
        try (FileInputStream fis = new FileInputStream(wordFile);
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

    protected void convertDocToPdfOfJodconverter(File wordFile, String outputPath) throws Exception{

        if(properties.getJodconverter().getMode() == JodconverterModeEnum.LOCAL) {
            if (properties.getJodconverter().getLocal().getOfficeHome() != null && new File(properties.getJodconverter().getLocal().getOfficeHome()).exists()) {

                // 启动 LibreOffice 服务
                OfficeManager officeManager = LocalOfficeManager.builder()
                        .officeHome(properties.getJodconverter().getLocal().getOfficeHome())  // 修改为你的 LibreOffice 路径
                        .install()
                        .build();
                try {
                    officeManager.start();
                    LocalConverter.make()
                            .convert(wordFile)
                            .to(new File(outputPath))
                            .execute();
                    System.out.println("PDF 转换成功！");
                } catch (Exception e) {
                    System.err.println("转换失败：" + e.getMessage());
                } finally {
                    officeManager.stop();
                }
            }
            else {
                convertDocToPdf(wordFile, outputPath);
            }
        }
        else if(properties.getJodconverter().getMode() == JodconverterModeEnum.REMOTE) {
            if(properties.getJodconverter().getRemote().getApiHost() == null) {
                convertDocToPdf(wordFile, outputPath);
            }
            else {
                remoteConvertToPdf(wordFile, new File(outputPath));
            }
        }
    }
}

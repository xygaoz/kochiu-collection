package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service("txt")
@FileType(thumb = true, mimeType = "text/plain", desc = ResourceTypeEnum.DOCUMENT)
public class TxtFileStrategy implements FileStrategy {

    /**
     * 生成缩略图
     *
     * @param file 文本文件
     * @param thumbFilePath 缩略图文件路径
     * @param thumbUrl 缩略图URL
     * @param fileType 文件类型注解
     * @param resourceDto 资源DTO
     * @return 缩略图比例
     * @throws Exception
     */
    @Override
    public String createThumbnail(File file,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileType fileType,
                                  ResourceDto resourceDto) throws Exception {

        // A4 纸尺寸（300 DPI）
        final int A4_WIDTH = 2480;
        final int A4_HEIGHT = 3508;
        // 边距设置
        final int MARGIN = 100;
        // 字体大小
        final int FONT_SIZE = 80;
        // 行距
        final int LINE_SPACING = 50;

        try {
            List<String> lines = Files.readAllLines(file.toPath());

            // 创建A4比例的图像
            BufferedImage image = new BufferedImage(A4_WIDTH, A4_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // 设置抗锯齿和高质量渲染
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 填充白色背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, A4_WIDTH, A4_HEIGHT);

            // 设置字体
            Font font = new Font("Microsoft YaHei", Font.PLAIN, FONT_SIZE);
            g2d.setFont(font);
            g2d.setColor(Color.BLACK);

            // 计算可显示的行数
            int maxLines = (A4_HEIGHT - 2 * MARGIN) / LINE_SPACING;
            int linesToShow = Math.min(lines.size(), maxLines);

            // 绘制文本内容
            int y = MARGIN + FONT_SIZE;
            for (int i = 0; i < linesToShow; i++) {
                String line = lines.get(i);
                // 如果行太长，进行截断
                if (line.length() > 100) {
                    line = line.substring(0, 100) + "...";
                }
                g2d.drawString(line, MARGIN, y);
                y += LINE_SPACING;
            }

            g2d.dispose();

            // 保存图像
            resourceDto.setThumbRatio(ImageUtil.writeThumbnail(image, thumbFilePath));
            resourceDto.setThumbUrl(thumbUrl);

            return resourceDto.getThumbRatio();
        } catch (IOException e) {
            log.error("TXT文件生成缩略图失败", e);
            throw new Exception("TXT文件生成缩略图失败", e);
        }
    }
}
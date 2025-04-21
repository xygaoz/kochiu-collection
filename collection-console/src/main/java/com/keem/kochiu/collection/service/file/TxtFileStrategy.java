package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service("txt")
public class TxtFileStrategy implements FileStrategy{

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

        // A4 纸尺寸（300 DPI）
        final int A4_WIDTH = 2480;
        final int A4_HEIGHT = 3508;

        try {
            List<String> lines = Files.readAllLines(file.toPath());

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
            resourceDto.setThumbRatio(ImageUtil.writeThumbnail(image, thumbFilePath));
            resourceDto.setThumbUrl(thumbUrl);

            return resourceDto.getThumbRatio();
        } catch (IOException e) {
            log.error("转换失败", e);
        }
        return null;
    }
}

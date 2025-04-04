package com.keem.kochiu.collection.service.file;

import cn.hutool.core.io.FileUtil;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service("unknown")
public class UnknownFileStrategy implements FileStrategy{

    @Override
    public String createThumbnail(String filePath,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        Resource resource = new ClassPathResource("/images/unknown.png");
        if (resource.exists()) {
            try {
                FileUtil.copyFile(resource.getInputStream(), new File(thumbFilePath), StandardCopyOption.REPLACE_EXISTING);
                resourceDto.setThumbUrl(thumbUrl);
                BufferedImage image = ImageIO.read(resource.getInputStream());
                resourceDto.setThumbRatio(image.getWidth() + "x" + image.getHeight());
                resourceDto.setThumbUrl(thumbUrl);

                return resourceDto.getThumbRatio();
            } catch (IOException e) {
                log.error("缩略图生成失败", e);
            }
        }

        return null;
    }
}

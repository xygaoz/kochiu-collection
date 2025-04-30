package com.keem.kochiu.collection.service.file;

import cn.hutool.core.io.FileUtil;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.StandardCopyOption;

public interface FileStrategy {

    String createThumbnail(File file, String thumbFilePath,
                           String thumbUrl,
                           FileTypeEnum fileType,
                           ResourceDto resourceDto) throws Exception;

    default String defaultThumbnail(String thumbFilePath,
                                    String thumbUrl,
                                    FileTypeEnum fileType,
                                    ResourceDto resourceDto) throws Exception{
        Resource resource = new ClassPathResource("/images/" + fileType.name() + ".png");
        if (resource.exists()) {
            FileUtil.copyFile(resource.getInputStream(), new File(thumbFilePath), StandardCopyOption.REPLACE_EXISTING);
            resourceDto.setThumbUrl(thumbUrl);
            BufferedImage image = ImageIO.read(resource.getInputStream());
            resourceDto.setThumbRatio(image.getWidth() + "x" + image.getHeight());
            resourceDto.setThumbUrl(thumbUrl);

            return resourceDto.getThumbRatio();
        }

        return null;
    }
}

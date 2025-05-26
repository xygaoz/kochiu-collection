package com.kochiu.collection.service.file;

import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
@Service("tif")
public class TifFileStrategy implements FileStrategy{


    @Override
    public String createThumbnail(File file, String thumbFilePath, String thumbUrl, FileTypeEnum fileType, ResourceDto resourceDto) throws Exception {

        try {
            String resolutionRation = null;
            //生成缩略图
            // 读取原始图片
            BufferedImage srcImg = ImageUtil.readImageWithFallback(file);
            if (fileType.isResolutionRatio()) {
                resolutionRation = srcImg.getWidth() + "x" + srcImg.getHeight();
            }

            resourceDto.setThumbRatio(ImageUtil.writeThumbnail(srcImg, thumbFilePath));
            resourceDto.setResolutionRatio(resolutionRation);
            resourceDto.setThumbUrl(thumbUrl);

            //预览文件，因为tit文件不能在浏览器直接预览
            ImageIO.write(srcImg, "png", new File(thumbFilePath.replace("_thumb.png", ".png")));
            resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".png"));

            return resourceDto.getThumbRatio();
        }
        catch (Exception e) {
            log.error("createThumbnail error", e);
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
    }
}

package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.util.ImageUtil;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;

@Service("jpg")
public class JpgFileStrategy implements FileStrategy{

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

        String resolutionRation = null;
        //生成缩略图
        // 读取原始图片
        BufferedImage srcImg = ImageUtil.readImageWithFallback(file);
        if(fileType.isResolutionRatio()){
            resolutionRation = srcImg.getWidth() + "x" + srcImg.getHeight();
        }

        resourceDto.setThumbRatio(ImageUtil.writeThumbnail(srcImg, thumbFilePath));
        resourceDto.setResolutionRatio(resolutionRation);
        resourceDto.setThumbUrl(thumbUrl);

        return resourceDto.getThumbRatio();
    }
}

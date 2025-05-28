package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.util.ImageUtil;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;

@Service("jpg")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/jpeg", desc = ResourceTypeEnum.IMAGE)
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
                                  FileType fileType,
                                  ResourceDto resourceDto) throws Exception {

        String resolutionRation = null;
        //生成缩略图
        // 读取原始图片
        BufferedImage srcImg = ImageUtil.readImageWithFallback(file);
        if(fileType.resolutionRatio()){
            resolutionRation = srcImg.getWidth() + "x" + srcImg.getHeight();
        }

        resourceDto.setThumbRatio(ImageUtil.writeThumbnail(srcImg, thumbFilePath));
        resourceDto.setResolutionRatio(resolutionRation);
        resourceDto.setThumbUrl(thumbUrl);

        return resourceDto.getThumbRatio();
    }
}

package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ApiModeEnum;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
@Service("dng")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-adobe-dng", desc = ResourceTypeEnum.IMAGE)
public class DngFileStrategy extends TifFileStrategy {

    @Autowired
    public DngFileStrategy(CollectionProperties collectionProperties) {
        super(collectionProperties);
    }

    @Override
    protected String getPreviewExtension() {
        return ".dng.png";
    }

    @Override
    protected boolean shouldUseImageMagick(File file) {
        // 总是使用ImageMagick处理
        return collectionProperties.getImageMagick().isEnabled();
    }

    @Override
    public String createThumbnail(File file, String thumbFilePath, String thumbUrl, FileType fileType, ResourceDto resourceDto) throws Exception {
        if (!collectionProperties.getImageMagick().isEnabled()) {
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
        return super.createThumbnail(file, thumbFilePath, thumbUrl, fileType, resourceDto);
    }

    @Override
    protected BufferedImage processImage(File file) throws Exception {
        if (!collectionProperties.getImageMagick().isEnabled()) {
            return ImageUtil.readImageWithFallback(file);
        }

        if (shouldUseImageMagick(file)) {
            if (collectionProperties.getImageMagick().getMode() == ApiModeEnum.REMOTE) {
                return processDngWithRemoteImageMagick(file);
            } else {
                return processDngWithLocalImageMagick(file);
            }
        }
        return super.processImage(file);
    }

    private BufferedImage processDngWithLocalImageMagick(File file) throws Exception {
        String tempOutput = File.createTempFile("dng_", ".png").getAbsolutePath();

        try {
            IMOperation op = new IMOperation();
            op.addImage(file.getAbsolutePath());
            op.quality(95.0); // DNG需要更高品质
            op.colorspace("sRGB"); // 确保色彩空间正确
            op.addImage(tempOutput);

            ConvertCmd cmd = new ConvertCmd();
            cmd.run(op);

            return ImageIO.read(new File(tempOutput));
        } finally {
            new File(tempOutput).delete();
        }
    }

    private BufferedImage processDngWithRemoteImageMagick(File file) throws Exception {

        String tempOutput = File.createTempFile("dng_", ".png").getAbsolutePath();
        try {
            generatePreviewWithRemoteImageMagick(file, tempOutput);
            return ImageIO.read(new File(tempOutput));
        } finally {
            new File(tempOutput).delete();
        }
    }
}
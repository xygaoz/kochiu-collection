package com.kochiu.collection.service.file;

import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.*;
import org.bytedeco.libraw.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;

import static org.bytedeco.libraw.global.LibRaw.*;

@Slf4j
@Service("dng")
public class DngFileStrategy implements FileStrategy {

    @Override
    public String createThumbnail(File file, String thumbFilePath, String thumbUrl, FileTypeEnum fileType, ResourceDto resourceDto) throws Exception {

        try {
            String resolutionRation = null;
            BufferedImage srcImg = processRaw(file);

            if (fileType.isResolutionRatio()) {
                resolutionRation = srcImg.getWidth() + "x" + srcImg.getHeight();
            }

            resourceDto.setThumbRatio(ImageUtil.writeThumbnail(srcImg, thumbFilePath));
            resourceDto.setResolutionRatio(resolutionRation);
            resourceDto.setThumbUrl(thumbUrl);

            // 生成预览图，raw文件不能直接预览
            ImageIO.write(srcImg, "png", new File(thumbFilePath.replace("_thumb.png", ".png")));
            resourceDto.setPreviewUrl(thumbUrl.replace("_thumb.png", ".png"));

            return resourceDto.getThumbRatio();
        }
        catch (Exception e) {
            log.error("Failed to create thumbnail for RAW file: " + file.getAbsolutePath(), e);
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
    }

    private BufferedImage processRaw(File file) {
        // 1. 初始化
        libraw_data_t rawData = libraw_init(0);
        if (rawData == null) {
            throw new RuntimeException("Failed to initialize LibRaw");
        }

        try {
            // 2. 加载文件
            int ret = libraw_open_file(rawData, file.getAbsolutePath());
            if (ret != 0) {  // LIBRAW_SUCCESS的值通常是0
                throw new RuntimeException("Failed to load RAW: " + libraw_strerror(ret));
            }

            // 3. 解码
            ret = libraw_unpack(rawData);
            if (ret != 0) {
                throw new RuntimeException("Failed to unpack: " + libraw_strerror(ret));
            }

            // 4. 创建内存图像
            try (IntPointer error = new IntPointer(1)) {
                libraw_processed_image_t processed = libraw_dcraw_make_mem_image(rawData, error);
                if (processed == null) {
                    throw new RuntimeException("Failed to process image: " + error.get());
                }
                return rawToBufferedImage(processed);
            }
        } finally {
            libraw_close(rawData);
        }
    }

    private BufferedImage rawToBufferedImage(libraw_processed_image_t processed) {
        int width = processed.width();
        int height = processed.height();
        BytePointer data = processed.data();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = data.get(y * width * 3 + x * 3) & 0xFF;
                int g = data.get(y * width * 3 + x * 3 + 1) & 0xFF;
                int b = data.get(y * width * 3 + x * 3 + 2) & 0xFF;
                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }
}
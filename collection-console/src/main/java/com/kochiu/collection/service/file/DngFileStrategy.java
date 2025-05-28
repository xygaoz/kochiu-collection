package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.*;
import org.bytedeco.libraw.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.bytedeco.libraw.global.LibRaw.*;

@Slf4j
@Service("dng")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-adobe-dng", desc = ResourceTypeEnum.IMAGE)
public class DngFileStrategy implements FileStrategy {

    @Override
    public String createThumbnail(File file, String thumbFilePath, String thumbUrl, FileType fileType, ResourceDto resourceDto) throws Exception {

        try {
            String resolutionRation = null;
            BufferedImage srcImg = processRaw(file);

            if (fileType.resolutionRatio()) {
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
        libraw_data_t rawData = libraw_init(0);
        if (rawData == null) {
            throw new RuntimeException("Failed to initialize LibRaw");
        }

        try {
            // 加载文件并设置参数
            int ret = libraw_open_file(rawData, file.getAbsolutePath());
            if (ret != 0) {
                throw new RuntimeException("Failed to load RAW: " + libraw_strerror(ret));
            }

            rawData.params().output_color(1);  // 1 = sRGB
            rawData.params().use_camera_wb(1);
            rawData.params().output_bps(8);
            rawData.params().user_qual(1);
            rawData.params().user_flip(0);

            // 解码和处理
            ret = libraw_unpack(rawData);
            if (ret != 0) {
                throw new RuntimeException("Failed to unpack: " + libraw_strerror(ret));
            }

            ret = libraw_dcraw_process(rawData);
            if (ret != 0) {
                throw new RuntimeException("Failed to process: " + libraw_strerror(ret));
            }
            if (rawData.sizes().width() <= 0 || rawData.sizes().height() <= 0) {
                throw new RuntimeException("Invalid image dimensions after processing");
            }
            rawData.params().output_bps(8); // 确保 8 位输出
            rawData.params().output_color(1); // sRGB
            rawData.params().no_auto_bright(1); // 禁用自动亮度调整

            // 生成内存图像
            try (IntPointer error = new IntPointer(1)) {
                libraw_processed_image_t processed = libraw_dcraw_make_mem_image(rawData, error);
                if (processed == null || error.get() != 0) {
                    throw new RuntimeException("Failed to make mem image: " + error.get());
                }
                log.debug("Image processed: {}x{}, colors={}",
                        processed.width(), processed.height(), processed.colors());
                return rawToBufferedImage(processed);
            }
        } finally {
            libraw_close(rawData);
        }
    }

    private BufferedImage rawToBufferedImage(libraw_processed_image_t processed) {
        int width = processed.width();
        int height = processed.height();
        int colors = processed.colors(); // 检查色彩通道数
        BytePointer data = processed.data();

        if (width <= 0 || height <= 0 || colors != 3 || data == null || data.capacity() == 0) {
            throw new RuntimeException(String.format(
                    "Invalid image data: width=%d, height=%d, colors=%d, data.capacity=%d",
                    width, height, colors, data.capacity()
            ));
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int offset = (y * width + x) * 3;
                int r = data.get(offset) & 0xFF;
                int g = data.get(offset + 1) & 0xFF;
                int b = data.get(offset + 2) & 0xFF;
                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }
}
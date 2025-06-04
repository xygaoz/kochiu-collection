package com.kochiu.collection.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageUtil {

    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

    // 支持 WebP/GIF 的增强读取方法
    public static BufferedImage readImageWithFallback(File file) throws IOException {
        try {
            if(file.getName().toLowerCase().endsWith(".tif")
                    || file.getName().toLowerCase().endsWith(".psd")
                    || file.getName().toLowerCase().endsWith(".psb")){
                return readTiffImage(file);
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            // 特殊格式回退处理
            if (file.getName().toLowerCase().endsWith(".webp")) {
                return readWebPImage(file);
            } else if (file.getName().toLowerCase().endsWith(".gif")) {
                return readGifFirstFrame(file);
            }
            throw e;
        }
    }

    // 读取 WebP 图像
    private static BufferedImage readWebPImage(File file) throws IOException {
        return ImageIO.read(file); // 依赖 webp-imageio-core
    }

    // 读取 GIF 第一帧
    private static BufferedImage readGifFirstFrame(File file) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(in);
                return reader.read(0);
            }
            throw new IOException("Unsupported GIF format");
        }
    }

    /**
     * 生成缩略图
     * @param image
     * @param outputPath
     * @throws IOException
     */
    public static String writeThumbnail(BufferedImage image, String outputPath) throws IOException {
        // 生成缩略图，参数依次为：原始图片，缩略图宽度，缩略图高度，是否等比缩放
        BufferedImage thumbnail = Thumbnails.of(image)
                .size(500, 500) // 设置缩略图的尺寸
                .outputQuality(1.0) // 设置输出质量，1.0为最高质量
                .antialiasing(Antialiasing.ON)  // 开启抗锯齿
                .rendering(Rendering.QUALITY)   // 高质量渲染模式
                .keepAspectRatio(true)
                .asBufferedImage();

        // 保存缩略图
        ImageIO.write(thumbnail, "png", new File(outputPath));
        log.debug("缩略图保存成功，尺寸为：{} x {}，路径: {}", thumbnail.getWidth(), thumbnail.getHeight(), outputPath);

        return thumbnail.getWidth() + "x" + thumbnail.getHeight();
    }

    public static BufferedImage readTiffImage(File file) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(in);

                // 只读取第一个IFD(图像文件目录)来获取缩略图
                return reader.read(0);
            }
            throw new IOException("No TIFF reader found");
        }
    }
}

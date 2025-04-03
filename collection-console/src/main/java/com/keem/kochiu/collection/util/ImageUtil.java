package com.keem.kochiu.collection.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageUtil {

    // 支持 WebP/GIF 的增强读取方法
    public static BufferedImage readImageWithFallback(String path) throws IOException {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            // 特殊格式回退处理
            if (path.toLowerCase().endsWith(".webp")) {
                return readWebPImage(path);
            } else if (path.toLowerCase().endsWith(".gif")) {
                return readGifFirstFrame(path);
            }
            throw e;
        }
    }

    // 读取 WebP 图像
    private static BufferedImage readWebPImage(String path) throws IOException {
        return ImageIO.read(new File(path)); // 依赖 webp-imageio-core
    }

    // 读取 GIF 第一帧
    private static BufferedImage readGifFirstFrame(String path) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new File(path))) {
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

        return thumbnail.getWidth() + "x" + thumbnail.getHeight();
    }
}

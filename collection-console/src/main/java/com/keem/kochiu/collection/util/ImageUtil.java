package com.keem.kochiu.collection.util;

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
}

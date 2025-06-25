package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ApiModeEnum;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Slf4j
@Service("tif")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/tiff", desc = ResourceTypeEnum.IMAGE)
public class TifFileStrategy implements FileStrategy {

    protected static final long LARGE_FILE_THRESHOLD = 209715200; // 200MB
    protected static final long HUGE_FILE_THRESHOLD = 1073741824; // 1GB
    protected final CollectionProperties collectionProperties;
    protected final RestTemplate restTemplate;

    public TifFileStrategy(CollectionProperties collectionProperties) {
        this.collectionProperties = collectionProperties;
        this.restTemplate = createRestTemplateWithTimeout();
        initializeImageMagick();
    }

    protected void initializeImageMagick() {
        if (collectionProperties.getImageMagick().isEnabled() &&
                collectionProperties.getImageMagick().getMode() == ApiModeEnum.LOCAL) {

            String imagemagickPath = collectionProperties.getImageMagick().getLocal().getImageMagickPath();

            String[] possiblePaths = {
                    imagemagickPath,
                    "/usr/local/bin",
                    "/usr/bin",
                    "/opt/homebrew/bin",
                    "C:\\Program Files\\ImageMagick-7.1.1-Q16-HDRI"
            };

            String runPath = null;
            for (String path : possiblePaths) {
                if (path == null) continue;

                // 优先检查magick命令
                if (new File(path, "magick").exists()) {
                    runPath = path;
                    System.setProperty("im4java.useGM", "false");
                    break;
                } else if (new File(path, "convert").exists()) {
                    runPath = path;
                    System.setProperty("im4java.useGM", "false");
                    break;
                } else if (new File(path, "gm").exists()) {
                    runPath = path;
                    System.setProperty("im4java.useGM", "true");
                    break;
                }
            }

            if (runPath != null && !runPath.isEmpty()) {
                log.info("Using ImageMagick at: {}", runPath);
                ConvertCmd.setGlobalSearchPath(runPath);
            } else {
                log.warn("ImageMagick not found in system PATH or configured locations");
            }
        }
    }

    @Override
    public String createThumbnail(File file, String thumbFilePath, String thumbUrl, FileType fileType, ResourceDto resourceDto) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            String resolutionRatio = null;
            BufferedImage srcImg = processImage(file);

            if (fileType.resolutionRatio()) {
                resolutionRatio = srcImg.getWidth() + "x" + srcImg.getHeight();
            }

            resourceDto.setThumbRatio(ImageUtil.writeThumbnail(srcImg, thumbFilePath));
            resourceDto.setResolutionRatio(resolutionRatio);
            resourceDto.setThumbUrl(thumbUrl);

            handlePreviewGeneration(file, srcImg, thumbFilePath, thumbUrl, resourceDto);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Processed {} ({} MB) in {} seconds",
                    file.getName(),
                    file.length()/1024/1024,
                    duration/1000.0);

            return resourceDto.getThumbRatio();
        } catch (Exception e) {
            log.error("createThumbnail error for file: {} ({} MB)",
                    file.getName(),
                    file.length()/1024/1024,
                    e);
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
    }

    protected BufferedImage processImage(File file) throws Exception {
        if (shouldUseImageMagick(file)) {
            if (collectionProperties.getImageMagick().getMode() == ApiModeEnum.REMOTE) {
                return processWithRemoteImageMagick(file);
            } else {
                return processWithLocalImageMagick(file, "tif");
            }
        }
        return ImageUtil.readImageWithFallback(file);
    }

    protected boolean shouldUseImageMagick(File file) {
        return collectionProperties.getImageMagick().isEnabled() &&
                file.length() > LARGE_FILE_THRESHOLD;
    }

    protected void handlePreviewGeneration(File file, BufferedImage srcImg,
                                           String thumbFilePath, String thumbUrl,
                                           ResourceDto resourceDto) throws Exception {
        String previewPath = thumbFilePath.replace("_thumb.png", getPreviewExtension());
        String previewUrl = thumbUrl.replace("_thumb.png", getPreviewExtension());

        if (shouldUseImageMagick(file)) {
            if (collectionProperties.getImageMagick().getMode() == ApiModeEnum.REMOTE) {
                generatePreviewWithRemoteImageMagick(file, previewPath);
            } else {
                generatePreviewWithLocalImageMagick(file, previewPath);
            }
        } else {
            generatePreviewWithThumbnailator(srcImg, previewPath);
        }

        resourceDto.setPreviewUrl(previewUrl);
    }

    protected String getPreviewExtension() {
        return ".tif.png";
    }

    protected void generatePreviewWithThumbnailator(BufferedImage srcImg, String previewPath) throws Exception {
        BufferedImage thumbnail = Thumbnails.of(srcImg)
                .size(2048, 2048)
                .outputQuality(1.0)
                .antialiasing(Antialiasing.ON)
                .rendering(Rendering.QUALITY)
                .keepAspectRatio(true)
                .asBufferedImage();
        ImageIO.write(thumbnail, "png", new File(previewPath));
    }

    protected BufferedImage processWithLocalImageMagick(File file, String format) throws Exception {
        log.info("Processing large file with ImageMagick: {} ({} MB)",
                file.getName(),
                file.length()/1024/1024);

        String tempOutput = File.createTempFile("convert_", ".png").getAbsolutePath();

        try {
            IMOperation op = new IMOperation();

            // 内存和性能优化参数
            op.addRawArgs("-limit", "memory", "4GiB");
            op.addRawArgs("-limit", "map", "8GiB");
            op.addRawArgs("-limit", "disk", "100GiB");

            // 大文件处理专用参数
            if (file.length() > HUGE_FILE_THRESHOLD) {
                op.addRawArgs("-define", "tiff:exif-properties=false");
                op.addRawArgs("-define", "tiff:ignore-tags=GainControl,Photoshop");
                op.addRawArgs("-quiet"); // 减少日志输出
                op.addRawArgs("-regard-warnings"); // 忽略警告继续处理
            } else {
                op.addRawArgs("-define", "tiff:ignore-tags=GainControl");
            }

            // 分块处理大图像
            op.addRawArgs("-define", "tiff:tile-geometry=256x256");
            op.addRawArgs("-depth", "8");

            op.addImage(file.getAbsolutePath() + "[0]");

            if ("tif".equals(format) || "psd".equals(format)) {
                op.flatten();
            }

            // 优化处理流程
            op.addRawArgs("-colorspace", "sRGB");
            op.addRawArgs("-strip");
            op.quality(90.0);
            op.addImage(tempOutput);

            ConvertCmd cmd = new ConvertCmd();
            try {
                long start = System.currentTimeMillis();
                cmd.run(op);
                long duration = System.currentTimeMillis() - start;

                log.debug("ImageMagick conversion completed in {} ms", duration);

                BufferedImage result = ImageIO.read(new File(tempOutput));
                if (result == null) {
                    throw new IOException("Failed to read converted image");
                }
                return result;
            } catch (Exception e) {
                log.error("ImageMagick conversion failed. Command: {}. Error output: {}",
                        cmd.getCommand(),
                        cmd.getErrorText(),
                        e);
                throw new RuntimeException("Image processing failed. Please check ImageMagick installation.", e);
            }
        } finally {
            new File(tempOutput).delete();
        }
    }

    protected BufferedImage processWithRemoteImageMagick(File file) throws Exception {
        String tempOutput = File.createTempFile("remote_", ".png").getAbsolutePath();
        try {
            generatePreviewWithRemoteImageMagick(file, tempOutput);
            return ImageIO.read(new File(tempOutput));
        } finally {
            new File(tempOutput).delete();
        }
    }

    protected void generatePreviewWithLocalImageMagick(File inputFile, String outputPath) throws Exception {
        IMOperation op = new IMOperation();

        // 添加大文件处理优化参数
        if (inputFile.length() > HUGE_FILE_THRESHOLD) {
            op.addRawArgs("-limit", "memory", "4GiB");
            op.addRawArgs("-limit", "map", "8GiB");
            op.addRawArgs("-define", "tiff:exif-properties=false");
        }

        op.addImage(inputFile.getAbsolutePath() + "[0]");
        op.flatten();
        op.resize(2048, 2048);
        op.quality(100.0);
        op.addImage(outputPath);

        ConvertCmd cmd = new ConvertCmd();
        cmd.run(op);
    }

    protected void generatePreviewWithRemoteImageMagick(File inputFile, String outputPath) throws Exception {
        log.info("Generating preview with remote ImageMagick for file: {} ({} MB)",
                inputFile.getName(),
                inputFile.length()/1024/1024);

        CollectionProperties.Remote remoteConfig = collectionProperties.getImageMagick().getRemote();
        String apiUrl = remoteConfig.getApiUrl();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(inputFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    apiUrl + "?format=png",
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                try (ByteArrayInputStream bis = new ByteArrayInputStream(response.getBody())) {
                    BufferedImage image = ImageIO.read(bis);
                    if (image == null) {
                        throw new IOException("Failed to decode remote image");
                    }
                    ImageIO.write(image, "png", new File(outputPath));
                }
            } else {
                throw new RuntimeException("Remote ImageMagick API request failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Remote ImageMagick API call failed for file: {}", inputFile.getName(), e);
            throw e;
        }
    }

    private RestTemplate createRestTemplateWithTimeout() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(collectionProperties.getImageMagick() == null ||
                        collectionProperties.getImageMagick().getRemote() == null ?
                        5000 :
                        collectionProperties.getImageMagick().getRemote().getTimeout())
                .setSocketTimeout(collectionProperties.getImageMagick() == null ||
                        collectionProperties.getImageMagick().getRemote() == null ?
                        5000 :
                        collectionProperties.getImageMagick().getRemote().getTimeout())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
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
import java.util.Collections;

@Slf4j
@Service("tif")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/tiff", desc = ResourceTypeEnum.IMAGE)
public class TifFileStrategy implements FileStrategy {

    protected static final long LARGE_FILE_THRESHOLD = 209715200; // 200MB
    protected final CollectionProperties collectionProperties;
    protected final RestTemplate restTemplate;

    public TifFileStrategy(CollectionProperties collectionProperties) {
        this.collectionProperties = collectionProperties;
        this.restTemplate = createRestTemplateWithTimeout();
        initializeImageMagick();
    }

    protected void initializeImageMagick() {
        if (collectionProperties.getGraphicsMagick().isEnabled() &&
                collectionProperties.getGraphicsMagick().getMode() == ApiModeEnum.LOCAL) {

            // 优先使用配置的路径，否则尝试自动检测
            String imagemagickPath = collectionProperties.getGraphicsMagick().getLocal().getGraphicsMagickPath();

            if (imagemagickPath == null || imagemagickPath.isEmpty()) {
                // 尝试常见安装路径
                String[] possiblePaths = {
                        "/usr/local/bin",
                        "/usr/bin",
                        "/opt/homebrew/bin",
                        "C:\\Program Files\\ImageMagick-7.1.1-Q16-HDRI"
                };

                for (String path : possiblePaths) {
                    if (new File(path, "convert").exists() || new File(path, "gm").exists()) {
                        imagemagickPath = path;
                        break;
                    }
                }
            }

            if (imagemagickPath != null && !imagemagickPath.isEmpty()) {
                // 优先使用ImageMagick的convert命令
                if (new File(imagemagickPath, "convert").exists()) {
                    System.setProperty("im4java.useGM", "false");
                } else {
                    System.setProperty("im4java.useGM", "true");
                }
                ConvertCmd.setGlobalSearchPath(imagemagickPath);
                log.info("ImageMagick initialized with path: {}", imagemagickPath);
            } else {
                log.warn("ImageMagick not found in system PATH or configured locations");
            }
        }
    }

    @Override
    public String createThumbnail(File file, String thumbFilePath, String thumbUrl, FileType fileType, ResourceDto resourceDto) throws Exception {
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

            return resourceDto.getThumbRatio();
        } catch (Exception e) {
            log.error("createThumbnail error", e);
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
    }

    protected BufferedImage processImage(File file) throws Exception {
        if (shouldUseImageMagick(file)) {
            if (collectionProperties.getGraphicsMagick().getMode() == ApiModeEnum.REMOTE) {
                return processWithRemoteImageMagick(file);
            } else {
                return processWithLocalImageMagick(file, "tif");
            }
        }
        return ImageUtil.readImageWithFallback(file);
    }

    protected boolean shouldUseImageMagick(File file) {
        return collectionProperties.getGraphicsMagick().isEnabled() &&
                file.length() > LARGE_FILE_THRESHOLD;
    }

    protected void handlePreviewGeneration(File file, BufferedImage srcImg,
                                           String thumbFilePath, String thumbUrl,
                                           ResourceDto resourceDto) throws Exception {
        String previewPath = thumbFilePath.replace("_thumb.png", getPreviewExtension());
        String previewUrl = thumbUrl.replace("_thumb.png", getPreviewExtension());

        if (shouldUseImageMagick(file)) {
            if (collectionProperties.getGraphicsMagick().getMode() == ApiModeEnum.REMOTE) {
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
        String tempOutput = File.createTempFile("convert_", ".png").getAbsolutePath();

        try {
            IMOperation op = new IMOperation();
            op.addImage(file.getAbsolutePath() + "[0]");
            if ("tif".equals(format) || "psd".equals(format)) {
                op.flatten();
            }
            op.quality(90.0);
            op.addImage(tempOutput);

            ConvertCmd cmd = new ConvertCmd();
            try {
                cmd.run(op);
                BufferedImage result = ImageIO.read(new File(tempOutput));
                if (result == null) {
                    throw new IOException("Failed to read converted image");
                }
                return result;
            } catch (Exception e) {
                log.error("ImageMagick conversion failed. Command: {}", cmd.getCommand(), e);
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
        op.addImage(inputFile.getAbsolutePath() + "[0]");
        op.flatten();
        op.resize(2048, 2048);
        op.quality(100.0);
        op.addImage(outputPath);

        ConvertCmd cmd = new ConvertCmd();
        cmd.run(op);
    }

    protected void generatePreviewWithRemoteImageMagick(File inputFile, String outputPath) throws Exception {
        CollectionProperties.Remote remoteConfig = collectionProperties.getGraphicsMagick().getRemote();
        String apiUrl = remoteConfig.getApiHost() + "/convert";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.IMAGE_PNG));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(inputFile));
            body.add("resize", "2048x2048");
            body.add("quality", "100");
            body.add("outputFormat", "png");
            body.add("flatten", "true");

            if (remoteConfig.getUsername() != null && remoteConfig.getPassword() != null) {
                headers.setBasicAuth(remoteConfig.getUsername(), remoteConfig.getPassword());
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                try (ByteArrayInputStream bis = new ByteArrayInputStream(response.getBody())) {
                    BufferedImage image = ImageIO.read(bis);
                    ImageIO.write(image, "png", new File(outputPath));
                }
            } else {
                throw new RuntimeException("Remote ImageMagick API request failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Remote ImageMagick API call failed", e);
            throw e;
        }
    }

    private RestTemplate createRestTemplateWithTimeout() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(collectionProperties.getGraphicsMagick().getRemote().getTimeout())
                .setSocketTimeout(collectionProperties.getGraphicsMagick().getRemote().getTimeout())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
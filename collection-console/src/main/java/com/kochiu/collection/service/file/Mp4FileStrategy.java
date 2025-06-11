package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ApiModeEnum;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import com.kochiu.collection.util.ImageUtil;
import com.kochiu.collection.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service("mp4")
@FileType(thumb = true, mimeType = "video/mp4", desc = ResourceTypeEnum.VIDEO)
public class Mp4FileStrategy implements FileStrategy{

    protected final CollectionProperties properties;
    private final RestTemplate restTemplate;

    public Mp4FileStrategy(CollectionProperties properties) {
        this.properties = properties;
        this.restTemplate = createRestTemplateWithTimeout();
    }

    @Override
    public String createThumbnail(File file,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileType fileType,
                                  ResourceDto resourceDto) throws Exception {
        if(!properties.getFfmpeg().isEnabled()){
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
        else {
            if(properties.getFfmpeg().getMode() == ApiModeEnum.LOCAL) {
                log.info("使用本地API将视频文件截帧");
                return localApiCaptureVideoFrame(file, thumbFilePath, thumbUrl, fileType, resourceDto);
            }
            else{
                try {
                    log.info("使用远程API将视频文件截帧");
                    return remoteApiCaptureVideoFrame(file, thumbFilePath, thumbUrl, fileType, resourceDto);
                } catch (Exception e) {
                    log.error("远程截帧失败", e);
                    return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
                }
            }
        }
    }

    // 使用本地API截取视频帧
    private String localApiCaptureVideoFrame(File videoFile,
                                             String outputPath,
                                             String thumbUrl,
                                             FileType fileType,
                                             ResourceDto resourceDto) throws Exception {
        String ffmpegPath;
        if(SysUtil.isRunningInDocker()){
            ffmpegPath = "/usr/local/bin";
            if(!new File(ffmpegPath + "/ffmpeg").exists()){
                ffmpegPath = properties.getFfmpeg().getLocal().getFfmpegPath();
            }
        }
        else{
            ffmpegPath = properties.getFfmpeg().getLocal().getFfmpegPath();
        }

        if (StringUtils.isNotBlank(ffmpegPath) && new File(ffmpegPath).exists()) {
            log.info("使用ffmpeg截帧mp4文件为图片");
            try {
                // 1. 获取视频时长
                double duration = getVideoDuration(videoFile, ffmpegPath);

                // 2. 计算最佳截取时间点（避免开头黑帧）
                double captureTime = Math.min(60, duration * 0.1); // 取视频前10%或5秒处

                // 3. 截取视频帧
                localCaptureVideoFrame(videoFile, outputPath, captureTime, ffmpegPath);

                // 4. 处理生成的缩略图
                BufferedImage image = ImageIO.read(new File(outputPath));
                if (image != null) {
                    resourceDto.setThumbRatio(ImageUtil.writeThumbnail(image, outputPath));
                    resourceDto.setThumbUrl(thumbUrl);
                    return resourceDto.getThumbRatio();
                }
            } catch (Exception e) {
                log.error("视频截帧失败，使用默认缩略图", e);
            }
        }
        return defaultThumbnail(outputPath, thumbUrl, fileType, resourceDto);
    }

    /**
     * 获取视频时长（秒）
     */
    private double getVideoDuration(File videoFile, String ffmpegPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath + "/ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoFile.getAbsolutePath()
        );

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String durationStr = reader.readLine();
            return durationStr != null ? Double.parseDouble(durationStr) : 0;
        } finally {
            process.destroy();
        }
    }

    /**
     * 截取视频指定时间点的帧
     */
    private void localCaptureVideoFrame(File videoFile,
                                        String outputPath,
                                        double captureTime,
                                        String ffmpegPath) throws Exception {
        // 将秒转换为HH:MM:SS格式
        String timeCode = String.format("%02d:%02d:%02d",
                (int) (captureTime / 3600),
                (int) ((captureTime % 3600) / 60),
                (int) (captureTime % 60));

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath + "/ffmpeg",
                "-ss", timeCode,             // 跳转到指定时间
                "-i", videoFile.getAbsolutePath(),
                "-vframes", "1",             // 只取1帧
                "-q:v", "2",                 // 输出质量（1-31，越小越好）
                "-vf", "scale='min(640,iw)':-2", // 限制宽度最大640px，保持比例
                "-y",                        // 覆盖输出文件
                outputPath
        );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("FFmpeg process exited with code " + exitCode);
        }
    }

    //  使用远程API截取视频帧
    private String remoteApiCaptureVideoFrame(File videoFile,
                                              String outputPath,
                                              String thumbUrl,
                                              FileType fileType,
                                              ResourceDto resourceDto) throws Exception {

        if(StringUtils.isNotBlank(properties.getFfmpeg().getRemote().getApiUrl())) {

            remoteCaptureFrame(videoFile, outputPath);
            // 4. 处理生成的缩略图
            BufferedImage image = ImageIO.read(new File(outputPath));
            if (image != null) {
                resourceDto.setThumbRatio(ImageUtil.writeThumbnail(image, outputPath));
                resourceDto.setThumbUrl(thumbUrl);
                return resourceDto.getThumbRatio();
            } else {
                return defaultThumbnail(outputPath, thumbUrl, fileType, resourceDto);
            }
        }
        return defaultThumbnail(outputPath, thumbUrl, fileType, resourceDto);
    }

    //  使用远程API截取视频帧
    private void remoteCaptureFrame(File videoFile, String outputPath) throws IOException {
        // 1. 准备请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(videoFile));

        // 2. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 3. 创建请求实体
        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        // 4. 发送请求
        ResponseEntity<byte[]> response = restTemplate.exchange(
                properties.getFfmpeg().getRemote().getApiUrl(),
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );

        // 5. 保存结果
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Files.write(Path.of(outputPath), response.getBody());
        } else {
            throw new RuntimeException("截图失败: " + response.getStatusCode());
        }
    }

    // 创建具有超时设置的RestTemplate
    private RestTemplate createRestTemplateWithTimeout() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(properties.getFfmpeg().getRemote().getTimeout())
                .setSocketTimeout(properties.getFfmpeg().getRemote().getTimeout())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}

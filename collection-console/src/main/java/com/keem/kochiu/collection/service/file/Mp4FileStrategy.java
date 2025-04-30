package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Slf4j
@Service("mp4")
public class Mp4FileStrategy implements FileStrategy{

    @Autowired
    protected CollectionProperties properties;
    @Autowired
    @Qualifier("jpg")
    protected JpgFileStrategy jpgFileStrategy;

    @Override
    public String createThumbnail(File file,
                                  String thumbFilePath,
                                  String thumbUrl,
                                  FileTypeEnum fileType,
                                  ResourceDto resourceDto) throws Exception {

        if (properties.getFfmpegPath() != null && new File(properties.getFfmpegPath()).exists()) {
            log.info("使用ffmpeg截帧mp4文件为图片");
            try {
                // 1. 获取视频时长
                double duration = getVideoDuration(file);

                // 2. 计算最佳截取时间点（避免开头黑帧）
                double captureTime = Math.min(60, duration * 0.1); // 取视频前10%或5秒处

                // 3. 截取视频帧
                captureVideoFrame(file, thumbFilePath, captureTime);

                // 4. 处理生成的缩略图
                BufferedImage image = ImageIO.read(new File(thumbFilePath));
                if (image != null) {
                    resourceDto.setThumbRatio(ImageUtil.writeThumbnail(image, thumbFilePath));
                    resourceDto.setThumbUrl(thumbUrl);
                    return resourceDto.getThumbRatio();
                }
            } catch (Exception e) {
                log.error("视频截帧失败，使用默认缩略图", e);
            }
        }
        return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
    }

    /**
     * 获取视频时长（秒）
     */
    private double getVideoDuration(File videoFile) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                properties.getFfmpegPath() + "/ffprobe",
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
    private void captureVideoFrame(File videoFile,
                                   String outputPath,
                                   double captureTime) throws Exception {
        // 将秒转换为HH:MM:SS格式
        String timeCode = String.format("%02d:%02d:%02d",
                (int) (captureTime / 3600),
                (int) ((captureTime % 3600) / 60),
                (int) (captureTime % 60));

        ProcessBuilder pb = new ProcessBuilder(
                properties.getFfmpegPath() + "/ffmpeg",
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
}

package com.keem.kochiu.collection.service.file;

import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.properties.CollectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

            try {
                // 获取视频总时长（秒）
                Process durationProcess = new ProcessBuilder(
                        properties.getFfmpegPath() + "/ffmpeg", "-i", file.getAbsolutePath(), "-f", "null", "-"
                ).redirectErrorStream(true).start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(durationProcess.getInputStream()));
                String line;
                double duration = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Duration:")) {
                        String[] tokens = line.split("Duration: |,");
                        String time = tokens[1].trim();
                        String[] parts = time.split(":");
                        duration = Double.parseDouble(parts[0]) * 3600 +
                                Double.parseDouble(parts[1]) * 60 +
                                Double.parseDouble(parts[2]);
                    }
                }
                durationProcess.waitFor();

                // 随机生成时间点（0 ~ duration 之间）
                double randomTime = Math.random() * duration;

                // 截取指定时间点的帧
                Process captureProcess = new ProcessBuilder(
                        "ffmpeg",
                        "-ss", String.valueOf(randomTime), // 跳转到随机时间
                        "-i", file.getAbsolutePath(),
                        "-vframes", "1",                  // 只取1帧
                        "-q:v", "10",                     // 输出质量（1-31，越小越好）
                        thumbFilePath
                ).start();
                captureProcess.waitFor();

                log.debug("帧已保存到: " + thumbFilePath);

                //再次生成缩略图
                resourceDto.setThumbRatio(jpgFileStrategy.createThumbnail(new File(thumbFilePath), thumbFilePath, thumbUrl, fileType, resourceDto));

            } catch (Exception e) {
                log.error("视频截帧失败", e);
            }

            resourceDto.setThumbUrl(thumbUrl);
            return resourceDto.getThumbRatio();
        }
        else {
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
    }
}

package com.keem.kochiu.collection.properties;

import com.keem.kochiu.collection.enums.ResourceTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Component
@ConfigurationProperties(prefix = "collection")
public class CollectionProperties {

    private Map<String, List<String>> uploadType;
    // office安装目录
    private String officeHome;
    // ffmpeg安装目录
    private String ffmpegPath;

    public Collection<String> getUploadTypes() {
        Set<String> values = new HashSet<>();
        uploadType.forEach((k, v) -> {
            values.addAll(v);
        });
        return values;
    }

    public ResourceTypeEnum getResourceType(String fileExt) {
        for (Map.Entry<String, List<String>> entry : uploadType.entrySet()) {
            if (entry.getValue().contains(fileExt)) {
                return ResourceTypeEnum.getByValue(entry.getKey());
            }
        }
        return ResourceTypeEnum.OTHER;
    }
}

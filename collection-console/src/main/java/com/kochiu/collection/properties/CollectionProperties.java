package com.kochiu.collection.properties;

import com.kochiu.collection.enums.ApiModeEnum;
import com.kochiu.collection.enums.ResourceTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Component
@ConfigurationProperties(prefix = "collection")
public class CollectionProperties {

    private Map<String, List<String>> uploadType;
    private Jodconverter jodconverter = new Jodconverter();
    private FFMpeg ffmpeg = new FFMpeg();

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

    @Data
    public static class Jodconverter {
        private boolean enabled;
        private String mode;
        private Local local;
        private Remote remote;

        public ApiModeEnum getMode() {
            return ApiModeEnum.getByMode(mode);
        }
    }

    @Data
    public static class FFMpeg {
        private boolean enabled;
        private String mode;
        private Local local;
        private Remote remote;

        public ApiModeEnum getMode() {
            return ApiModeEnum.getByMode(mode);
        }
    }

    @Data
    public static class Local {
        private String officeHome;
        private String ffmpegPath;
    }

    @Data
    public static class Remote {
        private String username;
        private String password = "";
        private String apiHost;
        private String apiUrl;
        private int timeout = 300000;
    }
}

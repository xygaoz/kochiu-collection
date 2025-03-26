package com.keem.kochiu.collection.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Component
@ConfigurationProperties(prefix = "collection")
public class CollectionProperties {

    private String uploadPath = "/";
    private Map<String, List<String>> uploadType;

    public Collection<String> getUploadTypes() {
        Set<String> values = new HashSet<>();
        uploadType.forEach((k, v) -> {
            values.addAll(v);
        });
        return values;
    }
}

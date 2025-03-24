package com.keem.kochiu.collection.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "collection")
public class CollectionProperties {

    private String uploadPath;
}

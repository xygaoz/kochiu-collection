package com.keem.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResourceVo {

    private Long resourceId;
    private String thumbnailUrl;
    private String sourceFileName;
    private String title;
    private String description;
    private String resolutionRatio;
    private String size;
    private Integer isPublic;
    private Integer star;
    private Integer width;
    private Integer height;
}

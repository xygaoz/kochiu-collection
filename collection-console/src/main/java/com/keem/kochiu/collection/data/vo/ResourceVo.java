package com.keem.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ResourceVo {

    private Long resourceId;
    private String thumbnailUrl;
    private String resourceUrl;
    private String sourceFileName;
    private String title;
    private String description;
    private String resolutionRatio;
    private Long size;
    private String fileType;
    private Integer isPublic;
    private Integer star;
    private Integer width;
    private Integer height;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

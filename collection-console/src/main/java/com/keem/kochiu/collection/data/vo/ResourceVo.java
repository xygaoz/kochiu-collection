package com.keem.kochiu.collection.data.vo;

import com.keem.kochiu.collection.data.dto.TagDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class ResourceVo {

    private Long resourceId;
    private String thumbnailUrl;
    private String resourceUrl;
    private String previewUrl;
    private String sourceFileName;
    private String title;
    private String description;
    private String resolutionRatio;
    private Long size;
    private String fileType;
    private String typeName;
    private String cateName;
    private String cataPath;
    private Integer isPublic;
    private Integer star;
    private Integer width;
    private Integer height;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String mimeType;
    private List<TagDto> tags;
}

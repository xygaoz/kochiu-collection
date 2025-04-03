package com.keem.kochiu.collection.data.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResourceDto {

    private int userId;
    private int cateId;
    private String sourceFileName;
    private String resourceUrl;
    private String fileExt;
    private String resolutionRatio;
    private Long size;
    private String thumbUrl;
    private String thumbRatio;
}

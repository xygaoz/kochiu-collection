package com.kochiu.collection.data.dto;

import com.kochiu.collection.enums.SaveTypeEnum;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResourceDto {

    private Long resourceId;
    private int userId;
    private long cateId;
    private String sourceFileName;
    private String resourceUrl;
    private String filePath;
    private String fileExt;
    private String resolutionRatio;
    private Long size;
    private String thumbUrl;
    private String thumbRatio;
    private String previewUrl;
    private String md5;
    private Long cataId;
    private SaveTypeEnum saveType = SaveTypeEnum.LOCAL;
}

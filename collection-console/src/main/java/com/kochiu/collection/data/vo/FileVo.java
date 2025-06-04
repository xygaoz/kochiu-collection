package com.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileVo {

    private Long resourceId;
    private String url;
    private String thumbnailUrl;
    //kb
    private String size;
    private String mimeType;
    private String resourceUrl;
}

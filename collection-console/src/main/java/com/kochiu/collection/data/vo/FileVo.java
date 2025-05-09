package com.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileVo {

    public String url;
    public String thumbnailUrl;
    //kb
    public String size;
    public String mimeType;
    public String resourceUrl;
}

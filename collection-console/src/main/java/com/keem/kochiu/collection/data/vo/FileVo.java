package com.keem.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileVo {

    public String url;
    //kb
    public String size;
    public String mimeType;
}

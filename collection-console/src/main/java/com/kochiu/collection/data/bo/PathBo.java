package com.kochiu.collection.data.bo;

import com.kochiu.collection.enums.ImportMethodEnum;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PathBo {
    private String path;
    private ImportMethodEnum importMethod;
}

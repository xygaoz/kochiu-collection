package com.keem.kochiu.collection.data.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class ResourceTypeVo {

    private String label;
    private String value;
}

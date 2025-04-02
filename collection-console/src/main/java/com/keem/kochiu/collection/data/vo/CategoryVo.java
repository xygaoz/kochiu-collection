package com.keem.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CategoryVo {
    private int sno;
    private String cateName;
}

package com.keem.kochiu.collection.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CatalogVo {
    private Integer id;
    private String label;
    private int level;
    private List<CatalogVo> children = new ArrayList<>();
}

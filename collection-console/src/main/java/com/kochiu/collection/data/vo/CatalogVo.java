package com.kochiu.collection.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CatalogVo {
    private Long id;
    private String label;
    private int level;
    private int sno;
    private Long parentId;
    private List<CatalogVo> children = new ArrayList<>();
}

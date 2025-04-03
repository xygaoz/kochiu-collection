package com.keem.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PageVo<T> {

    private int pageNum;
    private int pageSize;
    private long total;
    private int pages;
    private List<T> list;
}

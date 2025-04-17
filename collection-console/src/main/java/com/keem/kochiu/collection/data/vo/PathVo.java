package com.keem.kochiu.collection.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class PathVo {
    private String path;
    private List<PathInfo> pathInfo = new ArrayList();

    @AllArgsConstructor
    @Data
    public static class PathInfo{
        private int sno;
        private String cateName;
    }
}

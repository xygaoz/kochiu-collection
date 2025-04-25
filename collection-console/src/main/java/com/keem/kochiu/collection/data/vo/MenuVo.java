package com.keem.kochiu.collection.data.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVo {

    private String name;
    private String path;
    private String title;
    private String icon;
    private String iconType;
    private String style;
    private String redirect;
    private List<MenuVo> children;

}

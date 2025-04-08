package com.keem.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResInfoBo {

    @NotNull(message = "resourceId不能为空")
    private Integer resourceId;
    private String title;
    private String description;
    private Integer star;
}

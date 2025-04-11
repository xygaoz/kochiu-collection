package com.keem.kochiu.collection.data.bo;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Builder
@Data
public class ResInfoBo {

    @NotNull(message = "resourceId不能为空")
    private Long resourceId;
    private String title;
    private String description;
    private Integer star;
    private String tagName;
}

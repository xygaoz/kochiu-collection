package com.keem.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class MoveToCategoryBo {
    @NotNull(message = "分类Id不能为空！！！")
    private Long cateId;
    @NotNull(message = "资源Id不能为空！！！")
    @Size(min = 1, message = "资源Id不能为空！！！")
    private Long resourceIds;
}

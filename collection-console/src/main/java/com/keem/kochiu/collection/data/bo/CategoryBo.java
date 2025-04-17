package com.keem.kochiu.collection.data.bo;

import com.keem.kochiu.collection.annotation.Add;
import com.keem.kochiu.collection.annotation.Edit;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Builder
@Data
public class CategoryBo {

    @NotNull(groups = {Edit.class}, message = "分类ID不能为空！！！")
    private Long cateId;
    @NotNull(groups = {Add.class, Edit.class}, message = "分类名称不能为空！！！")
    private String cateName;
}

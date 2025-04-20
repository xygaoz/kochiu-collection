package com.keem.kochiu.collection.data.bo;

import com.keem.kochiu.collection.annotation.Add;
import com.keem.kochiu.collection.annotation.Edit;
import com.keem.kochiu.collection.annotation.Remove;
import com.keem.kochiu.collection.enums.RemoveEnum;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Builder
@Data
public class CatalogBo {

    @NotNull(groups = {Edit.class, Remove.class}, message = "目录ID不能为空！！！")
    private Long cateId;
    @NotNull(groups = {Add.class, Edit.class}, message = "目录名称不能为空！！！")
    private String cataName;
    @NotNull(groups = {Add.class, Edit.class}, message = "上级目录ID不能为空！！！")
    private Long parentId;
    private RemoveEnum removeType;
}

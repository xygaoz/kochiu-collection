package com.keem.kochiu.collection.data.bo;

import com.keem.kochiu.collection.annotation.Edit;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class MoveToBo {
    @NotNull(groups = {Edit.class}, message = "分类Id不能为空！！！")
    private Long cateId;
    @NotNull(message = "资源Id不能为空！！！")
    @Size(min = 1, message = "资源Id不能为空！！！")
    private List<Long> resourceIds;
    //true时彻底删除
    private boolean deleted;
}

package com.kochiu.collection.data.bo;

import com.kochiu.collection.annotation.Add;
import com.kochiu.collection.annotation.Remove;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class BatchTagBo {

    @NotNull(message = "资源Id不能为空！！！")
    @Size(min = 1, message = "资源Id不能为空！！！")
    private List<Long> resourceIds;
    @NotNull(groups = {Remove.class}, message = "标签Id不能为空！！！")
    private Long tagId;
    @NotNull(groups = {Add.class}, message = "标签名称不能为空！！！")
    private String tagName;
}

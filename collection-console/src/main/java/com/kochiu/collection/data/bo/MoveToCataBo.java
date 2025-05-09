package com.kochiu.collection.data.bo;

import com.kochiu.collection.annotation.Edit;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class MoveToCataBo {
    @NotNull(groups = {Edit.class}, message = "目录Id不能为空！！！")
    private Long cataId;
    @NotNull(message = "资源Id不能为空！！！")
    @Size(min = 1, message = "资源Id不能为空！！！")
    private List<Long> resourceIds;
}

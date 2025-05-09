package com.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class BatchUpdateBo {

    @NotNull(message = "资源Id不能为空！！！")
    @Size(min = 1, message = "资源Id不能为空！！！")
    private List<Long> resourceIds;
    private String title;
    private String description;
    private Boolean share;
}

package com.keem.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class batchUpdateBo {

    @NotNull
    @Size(min = 1)
    private List<Long> resourceIds;
    private String title;
    private String description;
}
